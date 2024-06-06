package nast.marsh.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
public class rabbitMQConfig {
    @Value("${rabbitmq.queue.email.name}")
    private String emailQueueName;

    @Value("${rabbitmq.queue.block.name}")
    private String blockQueueName;

    @Value("${rabbitmq.queue.complaint.name}")
    private String complaintQueueName;

    @Value("${rabbitmq.queue.secretKey.name}")
    private String secretKeyQueueName;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key.email.name}")
    private String emailRoutingKey;

    @Value("${rabbitmq.routing.key.block.name}")
    private String blockRoutingKey;

    @Value("${rabbitmq.routing.key.complaint.name}")
    private String complaintRoutingKey;

    @Value("${rabbitmq.routing.key.secretKey.name}")
    private String secretKeyRoutingKey;

    @Bean
    public Queue emailQueue(){
        return new Queue(emailQueueName);
    }

    @Bean
    public Queue blockQueue(){
        return new Queue(blockQueueName);
    }

    @Bean
    public Queue complainQueue(){return new Queue(complaintQueueName);}

    @Bean
    public Queue secretKeyQueue(){
        return new Queue(secretKeyQueueName);
    }

    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding emailBinding(){
        return BindingBuilder
                .bind(emailQueue())
                .to(exchange())
                .with(emailRoutingKey);
    }

    @Bean
    public Binding secretKeyBinding(){
        return BindingBuilder
                .bind(secretKeyQueue())
                .to(exchange())
                .with(secretKeyRoutingKey);
    }

    @Bean
    public Binding complaintBinding(){
        return BindingBuilder
                .bind(complainQueue())
                .to(exchange())
                .with(complaintRoutingKey);
    }

    @Bean
    public Binding blockUserBinding(){
        return BindingBuilder
                .bind(blockQueue())
                .to(exchange())
                .with(blockRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

}

