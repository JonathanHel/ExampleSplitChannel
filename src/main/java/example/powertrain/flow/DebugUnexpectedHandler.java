package example.powertrain.flow;

import lombok.CustomLog;
import org.springframework.integration.core.GenericHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@CustomLog
@Component
public class DebugUnexpectedHandler implements GenericHandler<Object> {


    @Override
    public Object handle(Object payload, MessageHeaders headers) {
        log.info("{}",headers.get("sequenceNumber"));
        return payload;
    }
}
