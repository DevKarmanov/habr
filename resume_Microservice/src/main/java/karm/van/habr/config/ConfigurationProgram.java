package karm.van.habr.config;

import karm.van.habr.entity.AdminKey;
import karm.van.habr.repo.AdminKeyRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Configuration
@EnableAsync
public class ConfigurationProgram {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean("threadPoolTaskExecutor")
    public TaskExecutor getTaskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(20);
        threadPoolTaskExecutor.setMaxPoolSize(100);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setThreadNamePrefix("Async-");
        return threadPoolTaskExecutor;
    }

    @Bean
    public CommandLineRunner initAdminKey(AdminKeyRepo adminKeyRepo){
        return args -> {
            if (!adminKeyRepo.existsById(1L)){
                AdminKey adminKey = AdminKey.builder()
                        .admin_registration_key(UUID.randomUUID())
                        .build();
                adminKeyRepo.save(adminKey);
            }
        };
    }
}
