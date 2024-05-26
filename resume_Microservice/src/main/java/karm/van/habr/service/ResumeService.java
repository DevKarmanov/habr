package karm.van.habr.service;

import io.minio.errors.*;
import karm.van.habr.entity.Comment;
import karm.van.habr.entity.ImageResume;
import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Resume;
import karm.van.habr.exceptions.ImageTroubleException;
import karm.van.habr.repo.CommentRepo;
import karm.van.habr.repo.ImageResumeRepo;
import karm.van.habr.repo.MyUserRepo;
import karm.van.habr.repo.ResumeRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {
    private final ResumeRepo resumeRepo;
    private final MyUserRepo myUserRepo;
    private final ImageResumeRepo imageResumeRepo;
    private final ImageCompressionService imageCompressionService;
    private final CommentRepo commentRepo;
    private final MinioServer minioServer;
    private final static String BUSKET_NAME = "resume-images";

    @Transactional
    public Resume getResume(Long id){
        return resumeRepo.findById(id).orElse(null);
    }

    @Transactional(rollbackFor = {ImageTroubleException.class, UsernameNotFoundException.class, IOException.class})
    public void createResume(Authentication authentication, String title, String description, MultipartFile[] files) throws ImageTroubleException, UsernameNotFoundException, IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Optional<MyUser> opt_user = myUserRepo.findByName(authentication.getName());

        if (opt_user.isEmpty()){
            throw new UsernameNotFoundException("Пользователь с таким именем не найден");
        } else if (files.length > 4){
            throw new ImageTroubleException("Слишком много изображений");
        }

        MyUser user = opt_user.get();

        Resume resume = Resume.builder()
                .title(title.trim())
                .description(description.trim())
                .author(user)
                .createdAt(LocalDate.now())
                .build();

        resumeRepo.saveAndFlush(resume);

        imageSave(files, resume, BUSKET_NAME);
    }

    private void imageSave(MultipartFile[] files, Resume resume, String bucketName) throws ImageTroubleException, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        ExecutorService executorService = Executors.newFixedThreadPool(files.length);

        List<Callable<String>> tasks = getCallableList(files, bucketName, resume.getId());

        minioServer.createBucketIfNotExist(BUSKET_NAME);

        try {
            List<Future<String>> results = executorService.invokeAll(tasks);

            for (Future<String> result : results) {
                ImageResume imageResume = ImageResume.builder()
                        .resume(resume)
                        .objectName(result.get())
                        .bucketName(bucketName)
                        .build();
                imageResumeRepo.save(imageResume);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            throw new ImageTroubleException("Какая-то проблема с обработкой изображений. Приносим свои извинения. Попробуйте перезагрузить страницу и предоставить новые");
        } finally {
            executorService.shutdown();
        }
    }

    private List<Callable<String>> getCallableList(MultipartFile[] files, String bucketName,Long resumeId) {
        List<Callable<String>> tasks = new ArrayList<>();

        for (MultipartFile file : files) {
            tasks.add(() -> {
                byte[] compressedImage = imageCompressionService.compressImage(file.getBytes(), file.getContentType());
                if (compressedImage == null) {
                    throw new RuntimeException();
                }
                InputStream inputStream = new ByteArrayInputStream(compressedImage);
                String fileName = generateNameForImage(file,resumeId);
                minioServer.uploadFile(bucketName, fileName, inputStream, compressedImage.length, file.getContentType());
                return fileName;
            });
        }
        return tasks;
    }

    private static String generateNameForImage(MultipartFile file,Long resumeId) {
        return UUID.randomUUID()+"_"+file.getOriginalFilename() + "_" + resumeId;
    }

    @Transactional
    public InputStream getMinioImage(Long resumeId, int imageId) throws Exception {
        Optional<Resume> resume = resumeRepo.findById(resumeId);
        if (resume.isEmpty()){
            throw new RuntimeException("Объявление не найдено");
        }
        List<ImageResume> images = imageResumeRepo.findByResume(resume.get());
        if (imageId < 0 || imageId >= images.size()) {
            throw new RuntimeException("Индекс вышел за пределы массива");
        }
        ImageResume imageResume = images.get(imageId);
        return minioServer.downloadFile(imageResume.getBucketName(), imageResume.getObjectName());
    }

    @Transactional
    public void deleteResume(long imageId,String pathLogin,Authentication authentication){
        if (!authentication.getName().equals(pathLogin)){
            throw new RuntimeException("Вы не имеете права удалять чужие карточки");
        }
        Optional<ImageResume> image_opt = imageResumeRepo.findById(imageId);
        image_opt.ifPresentOrElse(image->{
            imageResumeRepo.delete(image);
                    try {
                        minioServer.deleteFile(BUSKET_NAME,image.getObjectName());
                    } catch (Exception e) {
                        throw new RuntimeException("Что-то пошло не так при удалении");
                    }
                },
                ()->{throw new RuntimeException("Такая карточка не найдена");});
    }
    @Transactional
    public void patchResume(Authentication authentication,String login,Optional<String> title, Optional<String> description, Optional<MultipartFile[]> files, Long cardId) throws ImageTroubleException {
        if (files.isPresent() && files.get().length>4){
            throw new ImageTroubleException("Слишком много изображений");
        }else if (!authentication.getName().equals(login)){
            throw new RuntimeException("Вы не имеете права изменять чужие публикации");
        }
        Optional<Resume> resume_opt = resumeRepo.findById(cardId);

        resume_opt.ifPresent(resume -> {
            if (title.isPresent() && !title.get().trim().isEmpty()){
                resume.setTitle(title.get().trim());
            }
            if (description.isPresent() && !description.get().trim().isEmpty()){
                resume.setDescription(description.get().trim());
            }
            List<ImageResume> resumes = imageResumeRepo.findByResume(resume);
            if (files.isPresent() && resumes.size()+files.get().length-1>=4){
                try {
                    throw new ImageTroubleException("У вас уже "+resumes.size()+" из 4-х изображений. Добавьте поменьше");
                } catch (ImageTroubleException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
            if (files.isPresent() && files.get().length > 0 && files.get()[0].getSize() > 0){
                try {
                    imageSave(files.get(),resume,BUSKET_NAME);
                } catch (ImageTroubleException | InsufficientDataException | ServerException | ErrorResponseException |
                         IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException |
                         XmlParserException | InternalException e) {
                    throw new RuntimeException(e);
                }
            }
            resumeRepo.save(resume);
        });
    }

    @Transactional
    public void deleteCard(Long cardId){
        Optional<Resume> resume_opt = resumeRepo.findById(cardId);
        resume_opt.ifPresentOrElse(resume -> {
            List<ImageResume> images = imageResumeRepo.findByResume(resume);
            resumeRepo.delete(resume);
            try {
                minioServer.deleteFiles(BUSKET_NAME,images.stream().map(ImageResume::getObjectName).toList());
            } catch (Exception e) {
                throw new RuntimeException("Произошла ошибка при удалении");
            }
        },()->{throw new RuntimeException("Возникла проблема с удалением, проношу свои извинения");});
    }

    public int getPaginationQuantity(){
        double resumesQuantity = resumeRepo.findAll().size();
        double result = resumesQuantity/6;
        log.info(String.valueOf(result));
        return result>0?(int)Math.ceil(result):1;
    }



    //@Cacheable(value = "resumes", key = "#offset +'-'+ #limit +'-'+ #filter")
    @Transactional
    public Page<Resume> getAllResume(int offset, int limit,String filter){
        PageRequest pageRequest = PageRequest.of(offset,limit);
        if (filter == null){
            return resumeRepo.findAll(pageRequest);
        }else {
            return resumeRepo.findByTitleContaining(filter,pageRequest);
        }

    }

    //TODO: кэширование комментариев @Cacheable(value = "comments", key = "#id")
    @Transactional
    public List<Comment> getCommentsInPost(Long id) {
        Optional<Resume> resume = resumeRepo.findById(id);

        return resume.map(commentRepo::getCommentByResume).orElse(null);
    }

}
