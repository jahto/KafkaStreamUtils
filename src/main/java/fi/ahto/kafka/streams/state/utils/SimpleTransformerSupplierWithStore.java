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

/**
 *
 * @author Jouni Ahto
 * @param <K>   both incoming and returned key type, also for saving into state store
 * @param <V>   both incoming and returned value type, also for saving into state store
 */
public abstract class SimpleTransformerSupplierWithStore<K, V>
        extends TransformerSupplierWithStore<K, V, KeyValue<K, V>> {

    /**
     *
     * @param builder   StreamsBuilder to use for adding the statestore 
     * @param keyserde  Serde for persisting the key in the statestore
     * @param valserde  Serde for persisting the value in the statestore
     * @param storeName    statestore's name
     */
    public SimpleTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String storeName) {
        super(builder, keyserde, valserde, storeName);
    }

    /**
     *
     */
    public abstract class TransformerImpl
            extends TransformerSupplierWithStore<K, V, KeyValue<K, V>>.TransformerImpl
            implements TransformerWithStore<K, V, KeyValue<K, V>> {

        /**
         *
         * @param k
         * @param v1
         * @param v2
         * @return
         */
        @Override
        public KeyValue<K, V> transform(K k, V v1, V v2) {
            V newVal = transformValue(v1, v2);
            return KeyValue.pair(k, newVal);
        }

        /**
         *
         * @param v1
         * @param v2
         * @return
         */
        public abstract V transformValue(V v1, V v2);
    }
}
