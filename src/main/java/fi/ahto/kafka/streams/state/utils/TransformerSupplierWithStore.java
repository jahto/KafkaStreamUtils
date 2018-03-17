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
 * Partial implementation of interface TransformerSupplier having a statestore.
 * 
 * Inner class TransformerImpl provides a default implementation for interface Transformer
 that fetches the previous value with the key used from store, calls transform(key, previous, value)
 that you must provide yourself, and then saves the old value in the store. Override if you need something
 * fancier.
 * 
 * <pre class="code">
 * 
 *  &#064;Override
 *  public VR transform(K key, V current) {
 *      V previous = stateStore.get(key);
 *      VR transformed = transform(key, previous, current);
 *      stateStore.put(key, current);
 *      return transformed;
 *  }
 * </pre>
 * 
 * An example of possible usage using a string as the key and fictional classes InputData and TransformedData:
 * <pre class="code">
 *
 *  class MyTransformer extends TransformerSupplierWithStore&#60;String, InputData, KeyValue&#60;String, TransformedData>> {
 *
 *      public MyTransformer(StreamsBuilder builder, Serde&#60;String> keyserde, Serde&#;60InputData> valserde, String storeName) {
 *          super(builder, keyserde, valserde, storeName);
 *      }
 *
 *      &#064;Override
 *      public TransformerImpl createTransformer() {
 *          return new TransformerImpl() {
 *              &#064;Override
 *              public KeyValue&#60;String, TransformedData> transform(String key, InputData previous, InputData current) {
 *                  // Or do all the work here in case the transformation is very simple and can be done in a few lines.
 *                  return transformer(key, previous, current);
 *              }
 * 
 *              private KeyValue&#60;String, TransformedData> transformer(String key, InputData previous, InputData current) {
 *                  // Do something here to construct a TransformedData transformed and possibly a new key. Remember that previous can be null.
 *                  return KeyValue.pair(key, transformed);
 *              }
 *          };
 *      }
 *  }
 * 
 *  MyTransformer transformer = new MyTransformer(builder, Serdes.String(), inputSerde, STORE_NAME);
 *  KStream&#60String, InputData> streamin = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), inputSerde));
 *  KStream&#60String, TransformedData> streamout = streamin.transform(transformer, STORE_NAME);
 * </pre>
 *
 * @author Jouni Ahto
 * 
 * @param <K>   key type, also for saving into state store
 * @param <V>   value type, also for saving into state store
 * @param <VR>  return type
 */
public abstract class TransformerSupplierWithStore<K, V, VR extends KeyValue<?, ?>>
        implements TransformerSupplier<K, V, VR> {

    final private TransformerImpl transformer;

    /**
     *
     */
    final protected String storeName;

    /**
     *
     * @param builder   StreamsBuilder to use for adding the statestore 
     * @param keyserde  Serde for persisting the key in the statestore
     * @param valserde  Serde for persisting the value in the statestore
     * @param storeName    statestore's name
     */
    public TransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String storeName) {
        StoreBuilder<KeyValueStore<K, V>> store = Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(storeName),
                keyserde,
                valserde)
                .withCachingEnabled();

        builder.addStateStore(store);
        this.storeName = storeName;
        this.transformer = createTransformer();
    }

    /**
     * Instantiate your TransformerImpl here and override necessary abstract methods.
     * 
     * <pre class="code">
     * 
     * &#064;Override
     * TransformerImpl createTransformer() {
     *     return new TransformerImpl() {
     *         ...
     *     }
     * }
     * </pre>
     * 
     * @return  transformer implementation
     */
    protected abstract TransformerImpl createTransformer();

    /**
     * Return your TransformerImpl here.
     * 
     * @return  transformer implementation
     */
    @Override
    public Transformer<K, V, VR> get() {
        return transformer;
    }

    /**
     * Implementation of Transformer.
     */
    protected abstract class TransformerImpl implements TransformerWithStore<K, V, VR > {

        /**
         *
         */
        protected KeyValueStore<K, V> stateStore;

        @Override
        public void init(ProcessorContext pc) {
            stateStore = (KeyValueStore<K, V>) pc.getStateStore(storeName);
        }

        @Override
        public VR transform(K key, V current) {
            V previous = stateStore.get(key);
            VR transformed = transform(key, previous, current);
            stateStore.put(key, current);
            return transformed;
        }

        /**
         *
         * @param key
         * @param previous
         * @param current
         * @return
         */
        @Override
        public abstract VR transform(K key, V previous, V current);

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
