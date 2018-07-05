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
 * Partial implementation of interface ValueTransformerSupplier having a statestore.
 * <p>
 * A wrapper for ValueTransformerSupplierWithStore. Use this in case your transformed data has the same value type.
 * Useful when you only intend to add some missing information to an already existing value.
 *
 * @author Jouni Ahto
 * @param <K>   both incoming and returned key type, also for saving into state store
 * @param <V>   both incoming and returned value type, also for saving into state store
 */
public abstract class SimpleValueTransformerSupplierWithStore<K, V>
        extends ValueTransformerSupplierWithStore<K, V, V> {

    /**
     *
     * @param builder   StreamsBuilder to use for adding the statestore 
     * @param keyserde  Serde for persisting the key in the statestore
     * @param valserde  Serde for persisting the value in the statestore
     * @param storeName    statestore's name
     */
    public SimpleValueTransformerSupplierWithStore(final StreamsBuilder builder, final Serde<K> keyserde, final Serde<V> valserde, final String storeName) {
        super(builder, keyserde, valserde, storeName);
    }

    /**
     * Implementation of Transformer.
     */
    protected abstract class TransformerImpl
            extends ValueTransformerSupplierWithStore<K, V, V>.TransformerImpl
            implements ValueTransformerWithStore<K, V, V> {

        @Override
        public abstract V transform(final V current);

        /**
         *
         * @param previous
         * @param current
         * @return
         */
        @Override
        public abstract V transform(final V previous, final V current);

    }
}
