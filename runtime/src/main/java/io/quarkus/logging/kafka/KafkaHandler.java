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

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logmanager.ExtLogRecord;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static java.util.stream.Collectors.joining;

public class KafkaHandler extends Handler {

    private String appLabel;

    private final Emitter<String> emitter;

    public KafkaHandler(Emitter<String> emitter) {
        this.emitter = emitter;
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
        if (record.getParameters() != null && record.getParameters().length > 0) {
            switch (((ExtLogRecord) record).getFormatStyle()) {
                case PRINTF:
                    msg = String.format(record.getMessage(), record.getParameters());
                    break;
                case MESSAGE_FORMAT:
                    msg = MessageFormat.format(record.getMessage(), record.getParameters());
                    break;
                default: // == NO_FORMAT
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
        emitter.send(body);
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
}
