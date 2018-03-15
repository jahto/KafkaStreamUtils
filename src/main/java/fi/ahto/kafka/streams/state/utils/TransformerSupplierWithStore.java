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
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

/**
 *
 * @author Jouni Ahto
 * @param <K>
 * @param <V>
 * @param <VR>
 */

public abstract class TransformerSupplierWithStore<K, V, VR extends KeyValue<?, ?>>
        implements TransformerSupplier<K, V, VR> {
    
    final private TransformerImpl transformer;
    final protected String stateStoreName;

    public TransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String stateStoreName) {
        StoreBuilder<KeyValueStore<K, V>> store = Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(stateStoreName),
                keyserde,
                valserde)
                .withCachingEnabled();

        builder.addStateStore(store);
        this.stateStoreName = stateStoreName;
        this.transformer = createTransformer();
    }

    public abstract TransformerImpl createTransformer();

    @Override
    public Transformer<K, V, VR> get() {
        return transformer;
    }

    public abstract class TransformerImpl implements TransformerWithStore<K, V, VR> {

        protected KeyValueStore<K, V> stateStore;

        @Override
        public void init(ProcessorContext pc) {
            stateStore = (KeyValueStore<K, V>) pc.getStateStore(stateStoreName);
        }

        @Override
        public VR transform(K k, V v) {
            V oldVal = stateStore.get(k);
            VR newVal = transform(k, oldVal, v);
            stateStore.put(k, v);
            return newVal;
        }

        @Override
        public abstract VR transform(K k, V v1, V v2);

        @Override
        public VR punctuate(long l) {
            // Not needed and also deprecated.
            return null;
        }

        @Override
        public final void close() {
            // Note: The store should NOT be closed manually here via `stateStore.close()`!
            // The Kafka Streams API will automatically close stores when necessary.
        }
    }
}
