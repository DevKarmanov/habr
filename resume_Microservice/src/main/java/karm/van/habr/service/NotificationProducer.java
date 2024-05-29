package karm.van.habr.service;

import karm.van.habr.dto.NotificationDTO;
import karm.van.habr.entity.MyUser;
import karm.van.habr.repo.MyUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationProducer {
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key.email.name}")
    private String emailRoutingKey;

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


}
