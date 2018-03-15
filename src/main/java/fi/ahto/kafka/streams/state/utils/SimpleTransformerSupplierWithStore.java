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
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 *
 * @author Jouni Ahto
 * @param <K>
 * @param <V>
 */
public abstract class SimpleTransformerSupplierWithStore<K, V>
        extends TransformerSupplierWithStore<K, V, KeyValue<K, V>> {

    /**
     *
     * @param builder
     * @param keyserde
     * @param valserde
     * @param stateStoreName
     */
    public SimpleTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String stateStoreName) {
        super(builder, keyserde, valserde, stateStoreName);
    }
    
   public abstract class TransformerImpl
            extends TransformerSupplierWithStore<K, V, KeyValue<K, V>>.TransformerImpl
            implements TransformerWithStore<K, V, KeyValue<K, V>>

    //public abstract class TransformerImpl<K, V, VR extends KeyValue<K, V>>
    //        extends TransformerSupplierWithStore<K, V, VR>.TransformerImpl
    //        implements TransformerWithStore<K, V, VR>
    {

        @Override
        public KeyValue<K, V> transform(K k, V v1, V v2) {
            V newVal = transformValue(v1, v2);
            return KeyValue.pair(k, newVal);
        }

        public abstract V transformValue(V v1, V v2);
    }
}
