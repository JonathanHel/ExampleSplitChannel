package example.powertrain.config;

import example.powertrain.properties.ServiceProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.converter.MessagingMessageConverter;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class KafkaConfiguration {
    ServiceProperties serviceProperties;

    @Bean
    public NewTopic createSourceTopic() {
        return TopicBuilder.name(serviceProperties.getImportTopic().getName())
                .partitions(serviceProperties.getImportTopic().getPartitions())
                .replicas(serviceProperties.getImportTopic().getReplicas())
                .compact()
                .config(TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG, "0.01")
                .build();
    }


}
