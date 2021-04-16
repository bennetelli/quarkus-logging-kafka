package io.quarkus.logging.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
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
        this.testee = new KafkaHandler();
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

    @Nested
    class Payload {
        @Test
        void shouldConcatMessageAndTagsAndStacktrace() {
            String message = "some log output...";
            Map<String, String> tags = new HashMap<>();
            tags.put("level", "SEVERE");
            Throwable thrown = new RuntimeException("some runtime stacktrace");

            final String actualPayload = testee.assemblePayload(message, tags, thrown);

            assertTrue(actualPayload.contains("msg=[some log output...]"));
            assertTrue(actualPayload.contains("tags=[level=SEVERE]"));
            assertTrue(actualPayload.contains("stacktrace=[  "));
        }

        @Test
        void shouldNotAddTagsWhenTagsAreEmpty() {
            String message = "some log output...";

            final String actualPayload = testee.assemblePayload(message, new HashMap<>(), null);

            assertEquals("msg=[some log output...]", actualPayload);
        }

        @Test
        void shouldNotAddStacktrace() {
            String message = "some log output...";
            Map<String, String> tags = new HashMap<>();
            tags.put("level", "SEVERE");

            final String actualPayload = testee.assemblePayload(message, tags, null);

            assertEquals("msg=[some log output...], tags=[level=SEVERE]", actualPayload);
        }
    }
}
