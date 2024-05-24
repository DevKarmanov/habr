package karm.van.habr.service;


import io.minio.errors.*;
import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Settings;
import karm.van.habr.exceptions.ImageTroubleException;
import karm.van.habr.exceptions.UserAlreadyCreateException;
import karm.van.habr.repo.MyUserRepo;
import karm.van.habr.repo.SettingsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {

    private final MyUserRepo myUserRepo;
    private final ImageCompressionService imageCompressionService;
    private final SettingsRepo settingsRepo;
    private final MinioServer minioServer;
    private final static String BUSKET_NAME = "profile-images";

    @Transactional
    public void saveUser(String userName, String email, String password, MultipartFile file) throws UserAlreadyCreateException, ImageTroubleException {
        log.info("Вызван метод saveUser");
        if (myUserRepo.existsByName(userName)) {
            throw new UserAlreadyCreateException("Пользователь с таким именем уже существует. Придумайте другой");
        }

        saveImageAndUser(userName, email, password, file);
    }


    private void saveImageAndUser(String userName, String email, String password, MultipartFile file) throws ImageTroubleException {
        log.info("Вызван метод saveImageAndUser");
        try {
            byte[] imageBytes = imageCompressionService.compressImage(file.getBytes(),file.getContentType());
            if (imageBytes==null){
                throw new ImageTroubleException();
            }

            String fileName = UUID.randomUUID()+"_"+file.getOriginalFilename();

            MyUser myUser = MyUser.builder()
                    .name(userName)
                    .password(new BCryptPasswordEncoder(5).encode(password))
                    .email(email)
                    .busketName(BUSKET_NAME)
                    .objectName(fileName)
                    .role("ROLE_USER")
                    .build();

            myUserRepo.saveAndFlush(myUser);
            minioServer.createBucketIfNotExist(BUSKET_NAME);
            minioServer.uploadFile(BUSKET_NAME,fileName,file.getInputStream(), file.getSize(),file.getContentType());

            Settings settings = new Settings();
            settings.setUser(myUser);
            settings.setDontShowHint(false);
            settingsRepo.save(settings);

        } catch (ImageTroubleException | IOException | ServerException | InsufficientDataException |
                 ErrorResponseException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException |
                 XmlParserException | InternalException e) {
            log.error("Ошибка при чтении изображения: " + e.getMessage());
            throw new ImageTroubleException("Произошла ошибка с обработкой изображения. Приношу свои извинения. Попробуйте предоставить другое");
        }
    }


    @Transactional
    public void saveUserDetails(String firstname, String lastname, String description, String country, String jobtitle, String skillsInput, String name) throws UsernameNotFoundException {
        Optional<MyUser> user = myUserRepo.findByName(name);
        user.ifPresentOrElse(my_user -> {
            if (Stream.of(firstname, lastname, country, description, skillsInput, jobtitle)
                    .map(String::trim)
                    .anyMatch(String::isEmpty)) {
                throw new RuntimeException("Вы ввели что-то некорректно");
            }
            my_user.setFirstname(firstname);
            my_user.setLastname(lastname);
            my_user.setCountry(country);
            my_user.setDescription(description);
            my_user.setSkills(skillsInput);
            my_user.setRoleInCommand(jobtitle);
            myUserRepo.saveAndFlush(my_user);
        }, () -> {
            throw new UsernameNotFoundException("Пользователь с таким именем не найден");
        });
    }

    @Transactional(noRollbackFor = RuntimeException.class)
    public void patchUserDetails(Optional<String> firstname,Optional<String> lastname,Optional<String> description,
                                 Optional<String> country,Optional<String> jobTitle,Optional<String> skillsInput,String name,
                                 Optional<String> newLogin, Optional<String> email, Optional<MultipartFile> file){
        Pattern EMAIL_PATTERN = Pattern.compile(
                "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        );

        Optional<MyUser> user_opt = myUserRepo.findByName(name);
        user_opt.ifPresentOrElse(user->{
            ExecutorService executor = Executors.newFixedThreadPool(2);

            Callable<Void> task1 = () -> {
                email.map(String::trim).filter(s -> !s.isEmpty() && EMAIL_PATTERN.matcher(s).matches()).ifPresent(user::setEmail);
                firstname.map(String::trim).filter(s -> !s.isEmpty()).ifPresent(user::setFirstname);
                lastname.map(String::trim).filter(s -> !s.isEmpty()).ifPresent(user::setLastname);
                description.map(String::trim).filter(s -> !s.isEmpty()).ifPresent(user::setDescription);
                country.map(String::trim).filter(s -> !s.isEmpty()).ifPresent(user::setCountry);
                jobTitle.map(String::trim).filter(s -> !s.isEmpty()).ifPresent(user::setRoleInCommand);
                skillsInput.map(String::trim).filter(s -> !s.isEmpty()).ifPresent(user::setSkills);
                return null;
            };

            Callable<Void> task2 = () -> {
                if (file.isPresent() && file.get().getSize() > 0) {
                    String fileName = UUID.randomUUID()+"_"+file.get().getOriginalFilename();
                    try {
                        byte[] imageBytes = imageCompressionService.compressImage(file.get().getBytes(), file.get().getContentType());
                        if (imageBytes == null) {
                            throw new ImageTroubleException();
                        }
                        minioServer.deleteFile(BUSKET_NAME,user.getObjectName());
                        user.setObjectName(fileName);
                        minioServer.uploadFile(BUSKET_NAME,fileName,file.get().getInputStream(),file.get().getSize(),file.get().getContentType());
                    } catch (Exception e) {
                        throw new RuntimeException();
                    }
                }
                return null;
            };

            List<Callable<Void>> tasks = Arrays.asList(task1,task2);

            try {
                List<Future<Void>> futures = executor.invokeAll(tasks);
                for (Future<Void> future : futures){
                    future.get();
                }
            } catch (Exception e) {
                throw new RuntimeException("Произошла ошибка с обработкой изображения. Приношу свои извинения. Попробуйте предоставить другое");
            }

            myUserRepo.save(user);

            if (newLogin.isPresent() && !newLogin.get().trim().isEmpty()) {
                String newLoginValue = newLogin.get().trim();
                if (!name.equals(newLoginValue) && myUserRepo.existsByName(newLoginValue)) {
                    throw new RuntimeException("Логин уже занят, попробуйте другой");
                }
                user.setName(newLoginValue);
                myUserRepo.save(user);
            }

        },()->{throw new UsernameNotFoundException("Такой пользователь не найден");});
    }

    public InputStream getProfileImage(String name) throws Exception {
        Optional<MyUser> user = myUserRepo.findByName(name);

        if (user.isPresent()){
            return minioServer.downloadFile(user.get().getBusketName(),user.get().getObjectName());
        }else {
            throw new RuntimeException("Такой пользователь не найден");
        }
    }
}
