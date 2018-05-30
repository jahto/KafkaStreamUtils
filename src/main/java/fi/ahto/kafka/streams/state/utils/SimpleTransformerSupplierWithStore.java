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
 * Partial implementation of interface TransformerSupplier having a statestore.
 * <p>
 * A wrapper for TransformerSupplierWithStore. Use this in case your transformed data has the same key and value types
 * and you aren't going to change the key. Useful when you only intend to add some missing information to an already
 * existing value.
 *
 * <p>
 * Inner class TransformerImpl inherits transform(K key, V current) from TransformerSupplierWithStore.TransformerImpl and
 * provides a default implementation for transform(K key, V previous, V current) that makes sure you don't actually change the key.

 * <pre class="code">
 * &#064;Override
 * public KeyValue&#60;K, V&#60; transform(K key, V previous, V current) {
 *     V transformed = transformValue(previous, current);
 *     return KeyValue.pair(key, transformed);
 * }
 * 
 * // Provide your implementation of transformValue(previous, current).
 * </pre>
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
     * Implementation of Transformer.
     */
    protected abstract class TransformerImpl
            extends TransformerSupplierWithStore<K, V, KeyValue<K, V>>.TransformerImpl
            implements TransformerWithStore<K, V, KeyValue<K, V>> {

        /**
         *
         * @param key
         * @param previous
         * @param current
         * @return
         */
        @Override
        public KeyValue<K, V> transform(K key, V previous, V current) {
            V transformed = transformValue(previous, current);
            return KeyValue.pair(key, transformed);
        }

        /**
         *
         * @param previous
         * @param current
         * @return
         */
        protected abstract V transformValue(V previous, V current);
    }
}
