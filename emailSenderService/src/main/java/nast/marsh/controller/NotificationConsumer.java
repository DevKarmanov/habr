package nast.marsh.controller;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nast.marsh.dto.ComplaintDTO;
import nast.marsh.dto.NotificationDTO;
import nast.marsh.dto.SecretKeyDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

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
        sendMessage(String.valueOf(secretKeyDTO.secretKey()),"Секретный пароль",secretKeyDTO.email(),"Ваш секретный ключ, не разглашайте его никому!");
    }

    @RabbitListener(queues = {"${rabbitmq.queue.complaint.name}"})
    public void consumeComplaintDecision(ComplaintDTO complaintDTO) throws MessagingException {
        sendMessage(complaintDTO.description(),"Решение по вашей жалобе", complaintDTO.email(),"Недавно вы подали жалобу, и вот какое решение приняли модераторы: ");
    }

    @RabbitListener(queues = {"${rabbitmq.queue.block.name}"})
    public void banDescription(ComplaintDTO complaintDTO) throws MessagingException {
        sendMessage(complaintDTO.description(),"Блокировка аккаунта", complaintDTO.email(),"Ваш аккаунт заблокировали по причине: ", complaintDTO.unlockAt());
    }

    @RabbitListener(queues = {"${rabbitmq.queue.post.name}"})
    public void postBanDescription(ComplaintDTO complaintDTO) throws MessagingException {
        sendMessage(complaintDTO.description(),"Удаление вашей публикации", complaintDTO.email(),"Ваша публикация была удалена по причине: ");
    }

    public void sendMessage(String description,String subject, String email,String title) throws MessagingException {
        log.info("Данные для отправки: "+description);
        log.info("Отправляются на почту: "+email);

        String htmlMsg = "<html>" +
                "<body style='background-color: #f0f0f0; padding: 20px;'>" +
                "<h1 style='color: #333;'>"+title+"</h1>" +
                "<p style='color: #555;'>"+description+"</p>" +
                "</body>" +
                "</html>";

        send(email,subject,htmlMsg);
    }

    public void sendMessage(String description,String subject, String email,String title, String unlockAt) throws MessagingException {
        log.info("Данные для отправки: "+description);
        log.info("Отправляются на почту: "+email);

        String htmlMsg = "<html>" +
                "<body style='background-color: #f0f0f0; padding: 20px;'>" +
                "<h1 style='color: #333;'>"+title+"</h1>" +
                "<p style='color: #555;'>"+description+"</p>" +
                "<p style='color: #555;'>"+"Блокировка до: "+unlockAt+"</p>" +
                "<p style='color: #555;'>"+"Все ваши публикации стерты"+"</p>" +
                "</body>" +
                "</html>";

        send(email,subject,htmlMsg);
    }

    private void send(String email,String subject, String htmlMsg) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

        helper.setFrom(emailSender);
        helper.setTo(email);
        helper.setSubject(subject);
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
