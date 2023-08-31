package example.powertrain.flow;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;

import example.powertrain.properties.ServiceProperties;
import java.util.UUID;
import java.util.stream.IntStream;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringIntegrationTest
@SpringJUnitConfig
@Testcontainers
@SpringBootTest
class ImportFlowTest {


    @Container
    @ServiceConnection
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))
        .waitingFor(new HostPortWaitStrategy());

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> kafkaContainer.getHost() + ":" + kafkaContainer.getFirstMappedPort());
    }

    @Autowired
    MockIntegrationContext mockIntegrationContext;

    @Autowired
    KafkaTemplate<String, String> template;


    @Autowired
    ServiceProperties serviceProperties;


    @SpyBean
    DebugHandler debugHandler;

    @SpyBean
    DebugSecondHandler secondHandler;

    @SpyBean
    TombstoneHandler tombstoneHandler;

    @SpyBean
    DebugUnexpectedHandler unexpectedHandler;



    @Test
    void shouldAggregateData() {

        Headers headers = new RecordHeaders();
        IntStream.range(0, 100).forEach(x -> {
            publishData(headers, "example data");
        });

        Mockito.verify(debugHandler, timeout(10_000).times(100)).handle(any(), any());
        Mockito.verify(unexpectedHandler, timeout(10_000).times(0)).handle(any(), any());
        Mockito.verify(tombstoneHandler, timeout(10_000).times(0)).handle(any(), any());
        Mockito.verify(secondHandler, timeout(10_000).times(100)).handle(any(), any());

    }

    @Test
    void tombstoneHandling() {

        Headers headers = new RecordHeaders();
        IntStream.range(0, 100).forEach(x -> {
            publishData(headers, null);
        });

        Mockito.verify(debugHandler, timeout(10_000).times(0)).handle(any(), any());
        Mockito.verify(tombstoneHandler, timeout(10_000).times(100)).handle(any(), any());
        Mockito.verify(secondHandler, timeout(10_000).times(100)).handle(any(), any());
    }


    private void publishData(final Headers headers, String data) {

        template.send(
            new ProducerRecord<>(
                serviceProperties.getImportTopic().getName(),
                0,
                UUID.randomUUID().toString(),
                data,
                headers
            )
        );
    }
}