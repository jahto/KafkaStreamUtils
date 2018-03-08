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
// public abstract class TransformerSupplierWithStore<K, V1, KeyValue<K, VR1>>
//public abstract class TransformerSupplierWithStore<K, V, VR>
//        implements TransformerSupplier<K, V, KeyValue<K, VR>> {

public abstract class TransformerSupplierWithStore<K, V, KO, VO>
        implements TransformerSupplier<K, V, KeyValue<KO, VO>> {
    TransformerImpl transformer;

    public TransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserdein, Serde<VO> valserdeout, String stateStoreName) {
        StoreBuilder<KeyValueStore<K, V>> store = Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(stateStoreName),
                keyserde,
                valserdein)
                .withCachingEnabled();

        builder.addStateStore(store);
        this.transformer = createTransformer();
    }

    public abstract TransformerImpl createTransformer();

    @Override
    public Transformer<K, V, KeyValue<KO, VO>> get() {
        return transformer;
    }

    public static abstract class TransformerImpl<K1, V1, VR1> implements Transformer<K1, V1, VR1> {

        protected KeyValueStore<K1, V1> stateStore;
        protected String stateStoreName;

        public TransformerImpl(String name) {
            this.stateStoreName = name;
        }
        
        @Override
        public void init(ProcessorContext pc) {
            stateStore = (KeyValueStore<K1, V1>) pc.getStateStore(stateStoreName);
        }

        // @Override
        // public abstract VR1 transform(K1 k, V1 v);

        // public abstract VR1 transform(K1 k, V1 v1, V1 v2);

        @Override
        public VR1 punctuate(long l) {
            // Not needed and also deprecated.
            return null;
        }

        @Override
        public void close() {
            // Note: The store should NOT be closed manually here via `stateStore.close()`!
            // The Kafka Streams API will automatically close stores when necessary.
        }
    }
}
