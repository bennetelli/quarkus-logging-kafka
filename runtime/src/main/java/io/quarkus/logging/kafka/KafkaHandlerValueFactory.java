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

import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.TypeLiteral;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class KafkaHandlerValueFactory {

    Logger log = Logger.getLogger("LoggingKafka");

    public RuntimeValue<Optional<Handler>> create(final KafkaConfig config) {
        if (!config.enable) {
            log.fine("--- LogKafka is not enabled ---");

            return new RuntimeValue<>(Optional.empty());
        }

        InstanceHandle<Emitter<String>> emitter = Arc.container().instance(
                new TypeLiteral<Emitter<String>>() {
                },
                new ChannelLiteral("delivery"));

        KafkaHandler handler = new KafkaHandler(emitter.get());
        handler.setLevel(config.level);
        handler.setAppLabel(config.appLabel.orElse(""));

        return new RuntimeValue<>(Optional.of(handler));
    }

    static class ChannelLiteral extends AnnotationLiteral<org.eclipse.microprofile.reactive.messaging.Channel>
            implements Channel {

        private String value;

        public ChannelLiteral(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }
}
