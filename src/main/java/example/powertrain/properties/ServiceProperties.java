package example.powertrain.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "example")
@Data
@Validated
public class ServiceProperties {

    @NotNull
    Topic importTopic;

    @Data
    @Validated
    public static class Topic {
        @NotBlank
        String name;
        int partitions;
        int replicas;
    }

}
