/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.kafka.streams.utils;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.kstream.ValueTransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
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
public abstract class ValueTransformerSupplierWithStore<K, V, VR>
        implements ValueTransformerSupplier<V, VR> {
    
    private final String stateStoreName;
    private final ValueTransformerImpl<V, VR> transformer;
    private final StoreBuilder<KeyValueStore<K, V>> stateStore;

    /**
     *
     * @param builder
     * @param serdekey
     * @param serdein
     * @param serdeout
     * @param stateStoreName
     */
    public ValueTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> serdekey, Serde<V> serdein, Serde<VR> serdeout, String stateStoreName) {
        this.stateStoreName = stateStoreName;
        StoreBuilder<KeyValueStore<K, V>> store = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(stateStoreName),
                serdekey,
                serdein)
                .withCachingEnabled();

        builder.addStateStore(store);
        this.stateStore = store;
        this.transformer = createTransformer();
    }

    /**
     *
     * @return
     */
    public abstract ValueTransformerImpl<V, VR> createTransformer();

    /**
     *
     * @return
     */
    @Override
    public ValueTransformer<V, VR> get() {
        return transformer;
    }

    /**
     *
     * @param <V>
     * @param <VR>
     */
    public abstract class ValueTransformerImpl<V, VR> implements ValueTransformer<V, VR> {

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
         * @param v
         * @return
         */
        @Override
        public abstract VR transform(V v);

        /**
         *
         * @param oldVal
         * @param newVal
         * @return
         */
        public abstract VR transform(V oldVal, V newVal);

        /**
         *
         * @param l
         * @return
         */
        @Override
        public VR punctuate(long l) {
            // Not needed and also deprecated.
            return null;
        }

        /**
         *
         */
        @Override
        public void close() {
            // Note: The store should NOT be closed manually here via `stateStore.close()`!
            // The Kafka Streams API will automatically close stores when necessary.
        }
    }
}
