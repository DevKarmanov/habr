package karm.van.habr.service;

import karm.van.habr.dto.ComplaintDTO;
import karm.van.habr.dto.NotificationDTO;
import karm.van.habr.dto.SecretKeyDTO;
import karm.van.habr.entity.MyUser;
import karm.van.habr.repo.MyUserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key.email.name}")
    private String emailRoutingKey;

    @Value("${rabbitmq.routing.key.secretKey.name}")
    private String secretKeyRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    private final MyUserRepo myUserRepo;

    public void sendNotifications(Long parentUserId){
        Optional<MyUser> user_opt = myUserRepo.findById(parentUserId);
        user_opt.ifPresent(user->{
            if (!user.getSubscribers().isEmpty()){
                NotificationDTO notificationDTO = new NotificationDTO(user.getSubscribers().stream().map(MyUser::getEmail).toList());
                rabbitTemplate.convertAndSend(exchangeName,emailRoutingKey,notificationDTO);
            }
        });
    }

    public SecretKeyDTO sendSecretKey(String email){
        Random random = new Random();
        int secretKey = random.nextInt(1000000);
        SecretKeyDTO secretKeyDTO = new SecretKeyDTO(email,secretKey);
        rabbitTemplate.convertAndSend(exchangeName,secretKeyRoutingKey,secretKeyDTO);
        return secretKeyDTO;
    }

    public void sendComplaintDecision(String description, String authorEmail, String routingKey, String unlockAt){
        ComplaintDTO complaintDTO = new ComplaintDTO(authorEmail,description,unlockAt);
        log.info("Отправляется на mail: "+authorEmail);
        rabbitTemplate.convertAndSend(exchangeName,routingKey,complaintDTO);
    }


}
