package example;

import example.powertrain.properties.ServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({ServiceProperties.class})
@SpringBootApplication
public class ExampleService {
    public static void main(String[] args) {
        SpringApplication.run(ExampleService.class, args);
    }
}
