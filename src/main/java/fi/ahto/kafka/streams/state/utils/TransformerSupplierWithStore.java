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
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

/**
 *
 * @author Jouni Ahto
 * @param <K>
 * @param <V>
 */
public abstract class TransformerSupplierWithStore<K, V, VR>
        implements TransformerSupplier<K, V, KeyValue<K, VR>> {

    protected final String stateStoreName;
    protected final TransformerExtended<K, V, VR> transformer;

    /**
     *
     * @param builder
     * @param keyserde
     * @param valserde
     * @param stateStoreName
     */
    public TransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String stateStoreName) {
        this.stateStoreName = stateStoreName;
        StoreBuilder<KeyValueStore<K, V>> store = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(stateStoreName),
                keyserde,
                valserde)
                .withCachingEnabled();

        builder.addStateStore(store);
        this.transformer = createTransformer();
    }

    /**
     *
     * @return
     */
    public abstract TransformerImpl<K, V, VR> createTransformer();
    // public abstract TransformerImpl createTransformer();
    // public abstract TransformerExtended<K, V, VR> createTransformer();

    /**
     *
     * @return
     */
    @Override
    public Transformer<K, V, KeyValue<K, VR>> get() {
        return transformer;
    }
    /**
     *
     * @param <K>
     * @param <V>
     */
    protected abstract class TransformerImpl<K, V, VR> implements TransformerExtended<K, V, VR> {

        /**
         *
         */
        protected KeyValueStore<K, V> stateStore;

        /**
         *
         * @param pc
         */
        @Override
        public void init(ProcessorContext pc) {
            stateStore = (KeyValueStore<K, V>) pc.getStateStore(stateStoreName);
        }

        /**
         *
         * @param k
         * @param v
         * @return
         */
        /*
        @Override
        public abstract KeyValue<K, VR> transform(K k, V v);
        */
        /**
         *
         * @param k
         * @param v1
         * @param v2
         * @return
         */
        public abstract KeyValue<K, VR> transform(K k, V v1, VR v2);
    }
}
