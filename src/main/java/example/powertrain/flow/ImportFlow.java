package example.powertrain.flow;


import example.powertrain.properties.ServiceProperties;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.aggregator.MessageCountReleaseStrategy;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter.ListenerMode;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.KafkaNull;
import org.springframework.messaging.Message;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ImportFlow {


    ConsumerFactory<String, String> kafkaConsumerFactory;
    ServiceProperties serviceProperties;

    DebugHandler debugHandler;

    DebugSecondHandler secondHandler;

    DebugUnexpectedHandler unexpectedHandler;

    TombstoneHandler tombstoneHandler;

    @Bean
    public StandardIntegrationFlow startKafkaInbound() {
        return IntegrationFlow.from(Kafka
                .messageDrivenChannelAdapter(
                    kafkaConsumerFactory,
                    ListenerMode.record,
                    serviceProperties.getImportTopic().getName())
            )
            .channel(Objects.requireNonNull(routeKafkaMessage().getInputChannel()))
            .get();
    }

    @Bean
    public IntegrationFlow routeKafkaMessage() {
        return IntegrationFlow.from("routeKafkaMessage.input")
            .route(Message.class,
                p -> {
                    if (p.getPayload().equals(KafkaNull.INSTANCE)) {
                        return processTombstoneFlow().getInputChannel();
                    }
                    return someSplittingFlow().getInputChannel();
                })
            .get();
    }

    @Bean
    IntegrationFlow someSplittingFlow() {
        return IntegrationFlow.from("someSplittingFlow.input")
            .handle(debugHandler)
            .channel(sendToFlow().getInputChannel())
            .get();
    }


    @Bean
    IntegrationFlow processTombstoneFlow() {
        return IntegrationFlow.from("processTombstoneFlow.input")
            .handle(tombstoneHandler)
            .channel(sendToFlow().getInputChannel())
            .handle(unexpectedHandler)
            // .nullChannel()
            .handle(m -> {})
            .get();
    }


    @Bean
    IntegrationFlow sendToFlow() {
        return IntegrationFlow.from("sendToFlow.input")
            .handle(secondHandler)
            .handle(m -> {}, e -> e.id("endOfSendToFlow"))
            .get();
    }

}
