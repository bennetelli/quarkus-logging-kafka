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
import java.util.logging.Level;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = ConfigPhase.RUN_TIME, name = "log.kafka")
public class KafkaConfig {

    /**
     * Determine whether to enable the Loki logging extension.
     */
    @ConfigItem(name = ConfigItem.PARENT)
    boolean enable;

    /**
     * The Kafka Broker URL
     */
    @ConfigItem
    String brokerUrl;

    /**
     *
     */
    @ConfigItem
    String topicName;

    /**
     * App label
     *
     * If present, a label of app=\<appLabel> is supplied
     */
    @ConfigItem
    public Optional<String> appLabel;

    /**
     * The CW log level.
     */
    @ConfigItem(defaultValue = "WARN")
    public Level level;

}
