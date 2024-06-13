package karm.van.habr.service;

import karm.van.habr.entity.Complaint;
import karm.van.habr.entity.ImageResume;
import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Resume;
import karm.van.habr.helper.ImageService;
import karm.van.habr.repo.ComplaintRepo;
import karm.van.habr.repo.ImageResumeRepo;
import karm.van.habr.repo.MyUserRepo;
import karm.van.habr.repo.ResumeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComplaintService {
    private final ComplaintRepo complaintRepo;
    private final MyUserRepo myUserRepo;
    private final MinioServer minioServer;
    private final ImageResumeRepo imageResumeRepo;
    private final ImageCompressionService imageCompressionService;
    private final NotificationProducer notificationProducer;
    private final ResumeRepo resumeRepo;
    private final static String BUCKET_NAME = "complaint-images";
    private final static String IMAGE_BUCKET_NAME = "resume-images";


    @Value("${rabbitmq.routing.key.complaint.name}")
    private String complaintRoutingKey;

    @Value("${rabbitmq.routing.key.block.name}")
    private String banRoutingKey;

    @Value("${rabbitmq.routing.key.post.name}")
    private String postRoutingKey;

    @Transactional
    public void saveComplaint(String problemDescription, Optional<MultipartFile[]> files, String author_name, Optional<String> inspect_name,Optional<Long> inspect_card_id){
        myUserRepo.findByName(author_name).ifPresentOrElse(author->{
            Complaint complaint = new Complaint();
            complaint.setDescription(problemDescription);
            complaint.setCreatedAt(LocalDateTime.now());
            complaint.setAuthor(author);
            if (inspect_name.isPresent()){
                myUserRepo.findByName(inspect_name.get()).ifPresentOrElse(inspectUser->{
                    complaint.setInspectUser(inspectUser);
                    complaint.setType("USER");
                },()->{throw new RuntimeException("Пользователь, которого необходимо проверить, не найден");});
            }else inspect_card_id.ifPresent(aLong -> resumeRepo.findById(aLong).ifPresentOrElse(card -> {
                complaint.setInspectResume(card);
                complaint.setType("POST");
            }, () -> {
                throw new RuntimeException("Такая карточка не найдена");
            }));
            complaintRepo.save(complaint);
            try {
                if (files.isPresent() && files.get().length > 0 && files.get()[0].getSize() > 0){
                    ImageService.complaintImageSave(files.get(),complaint,BUCKET_NAME,minioServer,imageResumeRepo,imageCompressionService);
                }
            }catch (Exception e){
                throw new RuntimeException("Ошибка при обработке фотографии");
            }
        },()->{throw new RuntimeException("Такой пользователей не найден");});
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepo.findAll();
    }

    public InputStream getMinioImageComplaint(Long complaintId, int imageId) throws Exception {
        Optional<Complaint> complaint = complaintRepo.findById(complaintId);
        if (complaint.isEmpty()){
            throw new RuntimeException("Жалоба не найдена");
        }
        List<ImageResume> images = imageResumeRepo.findByComplaint(complaint.get());
        if (imageId < 0 || imageId >= images.size()) {
            throw new RuntimeException("Индекс вышел за пределы массива");
        }
        ImageResume imageResume = images.get(imageId);
        return minioServer.downloadFile(imageResume.getBucketName(), imageResume.getObjectName());
    }

    @Transactional
    public void dismissComplaint(String description, String authorEmail, Long complaintId) {
        deleteComplaint(complaintId);
        notificationProducer.sendComplaintDecision(description,authorEmail,complaintRoutingKey,null);
    }

    @Transactional
    public void deleteComplaint(Long complaintId){
        Optional<Complaint> complaint = complaintRepo.findById(complaintId);
        complaint.ifPresentOrElse(c->{
            List<String> imagesNames = imageResumeRepo.findByComplaint(c).stream().map(ImageResume::getObjectName).toList();
            try {
                minioServer.deleteFiles(BUCKET_NAME,imagesNames);
            } catch (Exception e) {
                throw new RuntimeException("Проблема с удалением фотографий");
            }
            imageResumeRepo.deleteAllByComplaint(c);
            complaintRepo.deleteById(complaintId);
        },()->{throw new RuntimeException("Такая жалоба не найдена");});
    }

    @Transactional
    public void deleteComplaint(Complaint complaint){
        List<String> imagesNames = imageResumeRepo.findByComplaint(complaint).stream().map(ImageResume::getObjectName).toList();
        try {
            minioServer.deleteFiles(BUCKET_NAME,imagesNames);
        } catch (Exception e) {
            throw new RuntimeException("Проблема с удалением фотографий");
        }
        imageResumeRepo.deleteAllByComplaint(complaint);
        complaintRepo.delete(complaint);
    }

    @Transactional
    public void successComplaint(String description, String authorEmail, Long complaintId, Optional<LocalDateTime> unlockAt, Optional<Long> inspectId) {
        Optional<MyUser> user = myUserRepo.findById(inspectId.get());
        user.ifPresentOrElse(u->{
            blockUser(unlockAt.get(),u);
            deleteComplaint(complaintId);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            notificationProducer.sendComplaintDecision(description,authorEmail,complaintRoutingKey,unlockAt.get().format(dateTimeFormatter));
            notificationProducer.sendComplaintDecision(description,u.getEmail(),banRoutingKey,unlockAt.get().format(dateTimeFormatter));
        },()->{throw new RuntimeException("Такой пользователь не найден");});
    }

    @Transactional
    public void successComplaint(String description, String authorEmail, Long complaintId, Optional<Long> inspectResumeId) {
        Optional<Resume> resume = resumeRepo.findById(inspectResumeId.get());
        resume.ifPresentOrElse(res->{
            deleteComplaint(complaintId);
            List<String> images = imageResumeRepo.findByResume(res).parallelStream().map(ImageResume::getObjectName).toList();
            try {
                minioServer.deleteFiles(IMAGE_BUCKET_NAME,images);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            notificationProducer.sendComplaintDecision(description,res.getAuthor().getEmail(),postRoutingKey,null);
            notificationProducer.sendComplaintDecision(description,authorEmail,complaintRoutingKey,null);
            imageResumeRepo.deleteAllByResume(res);
            resumeRepo.delete(res);
        },()->{throw new RuntimeException("Такой пост не найден");});
    }

    @Transactional
    public void successComplaint(String description,LocalDateTime unlockAt, Long userId) {
        Optional<MyUser> user = myUserRepo.findById(userId);
        user.ifPresentOrElse(u->{
            complaintRepo.findComplaintByInspectUser(u).ifPresent(this::deleteComplaint);
            resumeRepo.findByAuthor(u).parallelStream().forEach(resume-> complaintRepo.findComplaintByInspectResume(resume).ifPresent(this::deleteComplaint));
            blockUser(unlockAt,u);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            notificationProducer.sendComplaintDecision(description,u.getEmail(),banRoutingKey,unlockAt.format(dateTimeFormatter));
        },()->{throw new RuntimeException("Такой пользователь не найден");});
    }

    @Transactional
    public void blockUser(LocalDateTime unlockAt,MyUser user){
        user.setUnlockAt(unlockAt);
        user.setEnable(false);
        myUserRepo.save(user);
        resumeRepo.findByAuthor(user).parallelStream().forEach(resume->{
            List<String> images = imageResumeRepo.findByResume(resume)
                    .stream()
                    .map(ImageResume::getObjectName)
                    .toList();
            try {
                minioServer.deleteFiles(IMAGE_BUCKET_NAME,images);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            imageResumeRepo.deleteAllByResume(resume);
            resumeRepo.delete(resume);
        });
    }

    @Transactional
    public void unbanUser(Long userId) {
        Optional<MyUser> user = myUserRepo.findById(userId);
        user.ifPresentOrElse(s->{
            s.setEnable(true);
            s.setUnlockAt(LocalDateTime.now());
            myUserRepo.save(s);
        },()->{throw new RuntimeException("Такой пользователь не найден");});
    }
}
