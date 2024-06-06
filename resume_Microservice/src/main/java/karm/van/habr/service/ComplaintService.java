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

    @Transactional
    public void saveComplaint(String problemDescription, Optional<MultipartFile[]> files, String author_name, String inspect_name){

        myUserRepo.findByName(author_name).ifPresentOrElse(
                author->myUserRepo.findByName(inspect_name).ifPresentOrElse(inspect_user->{

                    Complaint complaint = Complaint.builder()
                            .description(problemDescription)
                            .author(author)
                            .inspect_user(inspect_user)
                            .createdAt(LocalDateTime.now())
                            .type("USER")
                            .build();
                    complaintRepo.save(complaint);
                    try {
                        if (files.isPresent()){
                            ImageService.complaintImageSave(files.get(),complaint,BUCKET_NAME,minioServer,imageResumeRepo,imageCompressionService);
                        }
                    }catch (Exception e){
                        throw new RuntimeException("Ошибка при обработке фотографии");
                    }

                    },()->{throw new RuntimeException("Такой пользователь не найден");}),
                ()->{throw new RuntimeException("Такой пользователь не найден");});
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepo.findAll();
    }

    public InputStream getMinioImageComplaint(Long complaintId, int imageId) throws Exception {
        Optional<Complaint> complaint = complaintRepo.findById(complaintId);
        if (complaint.isEmpty()){
            throw new RuntimeException("Жалоба не найдено");
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
    public void successComplaint(String description, String authorEmail, Long complaintId, LocalDateTime unlockAt, Long inspectId) {
        Optional<MyUser> user = myUserRepo.findById(inspectId);
        user.ifPresentOrElse(u->{
            blockUser(unlockAt,u);
            deleteComplaint(complaintId);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            notificationProducer.sendComplaintDecision(description,authorEmail,complaintRoutingKey,unlockAt.format(dateTimeFormatter));
            notificationProducer.sendComplaintDecision(description,u.getEmail(),banRoutingKey,unlockAt.format(dateTimeFormatter));
        },()->{throw new RuntimeException("Такой пользователь не найден");});
    }

    @Transactional
    public void successComplaint(String description,LocalDateTime unlockAt, Long userId) {
        Optional<MyUser> user = myUserRepo.findById(userId);
        user.ifPresentOrElse(u->{
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
}
