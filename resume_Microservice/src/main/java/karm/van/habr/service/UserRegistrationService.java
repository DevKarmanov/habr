package karm.van.habr.service;


import karm.van.habr.entity.MyUser;
import karm.van.habr.exceptions.ImageTroubleException;
import karm.van.habr.exceptions.UserAlreadyCreateException;
import karm.van.habr.repo.MyUserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {

    private final MyUserRepo myUserRepo;
    private final ImageCompressionService imageCompressionService;

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

            MyUser myUser = MyUser.builder()
                    .name(userName)
                    .password(new BCryptPasswordEncoder(5).encode(password))
                    .email(email)
                    .profileImage(imageBytes)
                    .imageType(file.getContentType())
                    .role("ROLE_USER")
                    .build();

            myUserRepo.saveAndFlush(myUser);
        } catch (ImageTroubleException | IOException e) {
            log.error("Ошибка при чтении изображения: " + e.getMessage());
            throw new ImageTroubleException("Произошла ошибка с обработкой изображения. Приношу свои извинения. Попробуйте перезагрузить страницу и предоставить другое");
        }
    }

    @Transactional
    public MyUser getUser(String name) {
        return myUserRepo.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с именем " + name + " не найден"));
    }


    @Transactional
    public void saveUserDetails(String firstname, String lastname, String description, String country, String jobtitle, String skillsInput, String name) throws UsernameNotFoundException {
        Optional<MyUser> user = myUserRepo.findByName(name);
        user.ifPresentOrElse(my_user -> {
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
}
