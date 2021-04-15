/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkus.logging.kafka;

import static java.util.stream.Collectors.joining;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.jboss.logmanager.ExtLogRecord;

import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

public class KafkaHandler extends Handler {

    private String appLabel;
    private String brokerUrl;
    private String topicName;
    private String keySerializer;
    private String valueSerializer;

    private final Map<String, String> config;

    private final KafkaProducer<String, String> producer;

    public KafkaHandler() {
        this.config = new HashMap<>();
        config.put("bootstrap.servers", "localhost:9092");
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("acks", "1");

        Vertx vertx = Vertx.vertx();
        this.producer = KafkaProducer.create(vertx, config);
    }

    @Override
    public void publish(LogRecord record) {
        // Skip messages that are below the configured threshold
        if (record.getLevel().intValue() < getLevel().intValue()) {
            return;
        }

        Map<String, String> tags = new HashMap<>();

        String host = record instanceof ExtLogRecord ? ((ExtLogRecord) record).getHostName() : null;
        if (record.getLoggerName().equals("__AccessLog")) {
            tags.put("type", "access");
        }
        if (host != null && !host.isEmpty()) {
            tags.put("host", host);
        }
        if (appLabel != null && !appLabel.isEmpty()) {
            tags.put("app", appLabel);
        }

        tags.put("level", record.getLevel().getName());

        String msg;
        if (record.getParameters() != null && record.getParameters().length > 0 && record instanceof ExtLogRecord) {
            ExtLogRecord.FormatStyle formatStyle = ((ExtLogRecord) record).getFormatStyle();// == NO_FORMAT
            if (formatStyle == ExtLogRecord.FormatStyle.PRINTF) {
                msg = String.format(record.getMessage(), record.getParameters());
            } else if (formatStyle == ExtLogRecord.FormatStyle.MESSAGE_FORMAT) {
                msg = MessageFormat.format(record.getMessage(), record.getParameters());
            } else {
                msg = record.getMessage();
            }
        } else {
            msg = record.getMessage();
        }

        if (record instanceof ExtLogRecord) {
            String tid = ((ExtLogRecord) record).getMdc("traceId");
            if (tid != null) {
                tags.put("traceId", tid);
            }
        }

        String body = assemblePayload(msg, tags, record.getThrown());

        config.remove("bootstrap.servers");
        config.put("bootstrap.servers", brokerUrl);

        if (keySerializer != null && !keySerializer.isEmpty()) {
            config.remove("key.serializer");
            config.put("key.serializer", keySerializer);
        }

        if (valueSerializer != null && !valueSerializer.isEmpty()) {
            config.remove("value.serializer");
            config.put("value.serializer", valueSerializer);
        }

        KafkaProducerRecord<String, String> records = KafkaProducerRecord.create(topicName, body);
        producer.write(records);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    private String assemblePayload(String message, Map<String, String> tags, Throwable thrown) {
        StringBuilder sb = new StringBuilder();
        sb.append("msg=[").append(message).append("]");
        if (thrown != null) {
            sb.append(", stacktrace=[");
            fillStackTrace(sb, thrown);
            sb.append("]");
        }
        if (!tags.isEmpty()) {
            sb.append(", tags=[");
            String tagsAsString = tags.keySet().stream()
                    .map(key -> key + "=" + tags.get(key))
                    .collect(joining(", "));
            sb.append(tagsAsString);
            sb.append("]");
        }
        return sb.toString();
    }

    private void fillStackTrace(StringBuilder sb, Throwable thrown) {
        for (StackTraceElement ste : thrown.getStackTrace()) {
            sb.append("  ").append(ste.toString()).append("\n");
        }
        if (thrown.getCause() != null) {
            sb.append("Caused by:");
            fillStackTrace(sb, thrown.getCause());
        }
    }

    void setAppLabel(String label) {
        if (label != null) {
            this.appLabel = label;
        }
    }

    void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    void setKeySerializer(String keySerializer) {
        this.keySerializer = keySerializer;
    }

    void setValueSerializer(String valueSerializer) {
        this.valueSerializer = valueSerializer;
    }
}
