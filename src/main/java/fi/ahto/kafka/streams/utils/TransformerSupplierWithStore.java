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

    private final String stateStoreName;
    private final TransformerImpl transformer;
    private final StoreBuilder<KeyValueStore<K, V>> stateStore;

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
        this.stateStore = store;
        this.transformer = createTransformer();
    }

    /**
     *
     * @return
     */
    public abstract TransformerImpl createTransformer();
    // public abstract TransformerImpl createTransformer();

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
    public abstract class TransformerImpl implements Transformer<K, V, KeyValue<K, VR>> {

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
        public abstract KeyValue<K, VR> transform(K k, V v);

        /**
         *
         * @param k
         * @param v1
         * @param v2
         * @return
         */
        public abstract KeyValue<K, V> transform(K k, V v1, V v2);

        /**
         *
         * @param l
         * @return
         */
        @Override
        public KeyValue<K, VR> punctuate(long l) {
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
