/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.ahto.kafka.streams.state.utils;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;

/**
 *
 * @author Jouni Ahto
 * @param <K>
 * @param <V>
 */
public abstract class SimpleValueTransformerSupplierWithStore<K, V>
        extends ValueTransformerSupplierWithStore<K, V, V>
{
    
    /**
     *
     * @param builder
     * @param serdekey
     * @param serdeval
     * @param stateStoreName
     */
    public SimpleValueTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> serdekey, Serde<V> serdeval, String stateStoreName) {
        super(builder, serdekey, serdeval, serdeval, stateStoreName);
    }
    
}
