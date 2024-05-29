package karm.van;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ImageCompressService
{
    public static void main( String[] args )
    {
        SpringApplication.run(ImageCompressService.class,args);
    }
}
