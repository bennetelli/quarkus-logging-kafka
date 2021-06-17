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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class KafkaHandler extends Handler {

    private static final String DEFAULT_STRING_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    private static final String DEFAULT_BROKER_URL = "localhost:9092";
    private static final String DEFAULT_ACKS = "1";

    private String appLabel;
    private String brokerUrl;
    private String topicName;
    private String keySerializer;
    private String valueSerializer;

    //    private final KafkaProducer<String, String> producer;

    private final KafkaConfig kafkaConfig;

    public KafkaHandler(KafkaConfig kafkaConfig) {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", DEFAULT_BROKER_URL);
        config.put("key.serializer", DEFAULT_STRING_SERIALIZER);
        config.put("value.serializer", DEFAULT_STRING_SERIALIZER);
        config.put("acks", DEFAULT_ACKS);

        //        this.producer = KafkaProducer.create(Vertx.vertx(), config);

        this.kafkaConfig = kafkaConfig;
    }

    @Override
    public void publish(LogRecord record) {
        // Skip messages that are below the configured threshold
        if (record.getLevel().intValue() < getLevel().intValue()) {
            return;
        }

        System.out.println("GETMESSAGE: " + record.getMessage());
        if (record.getMessage().equals("test warning")) {
            System.out.println("test warning kafka");
        }
        //        ElasticCommonSchemaLogFormatter elasticCommonSchemaLogFormatter = new ElasticCommonSchemaLogFormatter(kafkaConfig);
        //        String bodyECS = elasticCommonSchemaLogFormatter.format(record);
        //
        //        KafkaProducerRecord<String, String> records = KafkaProducerRecord.create(topicName, bodyECS);
        //        producer.write(records);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
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
