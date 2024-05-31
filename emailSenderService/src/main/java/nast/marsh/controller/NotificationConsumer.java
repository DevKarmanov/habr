package nast.marsh.controller;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nast.marsh.dto.NotificationDTO;
import nast.marsh.dto.SecretKeyDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.sender}")
    private String emailSender;

    @RabbitListener(queues = {"${rabbitmq.queue.email.name}"})
    public void consume(NotificationDTO dto) throws MessagingException {
        List<String> emails = dto.emails();
        if (!emails.isEmpty()){
            sendEmail(emails);
        }
    }

    @RabbitListener(queues = {"${rabbitmq.queue.secretKey.name}"})
    public void consumeSecretKey(SecretKeyDTO secretKeyDTO) throws MessagingException {
        sendSecretKey(secretKeyDTO);
    }

    public void sendSecretKey(SecretKeyDTO secretKeyDTO) throws MessagingException {
        log.info("Данные секретного ключа: "+secretKeyDTO.toString());
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

        String htmlMsg = "<html>" +
                "<body style='background-color: #f0f0f0; padding: 20px;'>" +
                "<h1 style='color: #333;'>Ваш секретный ключ</h1>" +
                "<p style='color: #555;'>Не разглашайте его никому!</p>" +
                "<p style='color: #555;'>"+secretKeyDTO.secretKey()+"</p>" +
                "</body>" +
                "</html>";

        helper.setFrom(emailSender);
        helper.setTo(secretKeyDTO.email());
        helper.setSubject("Ключ восстановления доступа к аккаунту");
        helper.setText(htmlMsg,true);
        javaMailSender.send(message);

    }

    public void sendEmail(List<String> emails) throws MessagingException {
        log.info("Почты для рассылки: "+emails);
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

        String htmlMsg = "<html>" +
                "<body style='background-color: #f0f0f0; padding: 20px;'>" +
                "<h1 style='color: #333;'>Новая публикация</h1>" +
                "<p style='color: #555;'>Пользователь, на которого вы подписаны, только что выложил новое объявление!</p>" +
                "</body>" +
                "</html>";

        helper.setFrom(emailSender);
        helper.setTo(emails.toArray(new String[0]));
        helper.setSubject("Новая публикация");
        helper.setText(htmlMsg,true);
        javaMailSender.send(message);

    }

}
