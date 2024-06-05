package karm.van.habr.service;

import karm.van.habr.entity.Complaint;
import karm.van.habr.entity.ImageResume;
import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Resume;
import karm.van.habr.helper.ImageService;
import karm.van.habr.repo.ComplaintRepo;
import karm.van.habr.repo.ImageResumeRepo;
import karm.van.habr.repo.MyUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
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
    private final static String BUKET_NAME = "complaint-images";

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
                            ImageService.complaintImageSave(files.get(),complaint,BUKET_NAME,minioServer,imageResumeRepo,imageCompressionService);
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
        notificationProducer.sendComplaintDecision(description,authorEmail);
    }

    public void deleteComplaint(Long complaintId){
        Optional<Complaint> complaint = complaintRepo.findById(complaintId);
        complaint.ifPresentOrElse(c->{
            List<String> imagesNames = imageResumeRepo.findByComplaint(c).stream().map(ImageResume::getObjectName).toList();
            try {
                minioServer.deleteFiles(BUKET_NAME,imagesNames);
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
            u.setUnlockAt(unlockAt);
            u.setEnable(false);
            myUserRepo.save(u);
            deleteComplaint(complaintId);
            notificationProducer.sendComplaintDecision(description,authorEmail);
        },()->{throw new RuntimeException("Такой пользователь не найден");});
    }
}
