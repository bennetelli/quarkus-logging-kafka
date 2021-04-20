package io.quarkus.logging.kafka;

import static org.mockito.Mockito.*;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaHandlerTest {

    private final KafkaHandler testee;

    public KafkaHandlerTest() {
        this.testee = new KafkaHandler(new KafkaConfig());
    }

    @Nested
    class Publish {
        @Test
        void shouldSkipLogsBelowTheConfiguredThreshold() {
            final KafkaHandler spy = spy(testee);
            LogRecord record = new LogRecord(Level.INFO, "some info message");
            record.setLoggerName("someLoggerName");
            testee.setLevel(Level.WARNING);
            testee.publish(record);

            verify(spy, times(0)).publish(any());
        }
    }
}
