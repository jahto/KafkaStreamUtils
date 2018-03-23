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
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.kstream.ValueTransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

/**
 * Partial implementation of interface ValueTransformerSupplier having a statestore.
 * <p>
 * An example of possible usage using a string as the key and fictional classes InputData and TransformedData:

* <pre class="code">
 * 
 *   class MyValueTransformer extends ValueTransformerSupplierWithStore&#60;String, InputData, TransformedData> {
 *
 *       public MyValueTransformer(StreamsBuilder builder, Serde&#60;String> keyserde, Serde&#60;InputData> valserde, String stateStoreName) {
 *           super(builder, keyserde, valserde, stateStoreName);
 *       }
 *
 *       &#064;Override
 *       public TransformerImpl createTransformer() {
 *           return new TransformerImpl() {
 *               &#064;Override
 *               public TransformedData transform(InputData current) {
 *                   InputData previous = stateStore.get(current.possiblekey);
 *                   TransformedData transformed = transform(previous, current);
 *                   stateStore.put(current.possiblekey, current);
 *                   return transformed;
 *               }
 *
 *               &#064;Override
 *               public TransformedData transform(InputData previous, InputData current) {
 *                   // Or do all the work here in case the transformation is very simple and can be done in a few lines.
 *                   return transformer(previous, current);
 *               }
 *               
 *               private TransformedData transformer(InputData previous, InputData current) {
 *                   // Do something here to construct a TransformedData transformed. Remember that previous can be null.
 *                   return transformed.
 *               }
 *           };
 *       }
 *   }
 * 
 *   MyValueTransformer transformer = new MyValueTransformer(builder, Serdes.String(), inputSerde, STORE_NAME);
 *   KStream&#60;String, InputData> streamin = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), inputSerde));
 *   KStream&#60;String, TransformedData> streamout = streamin.transform(transformer, STORE_NAME);
 * </pre>
 *
 * @author Jouni Ahto
 * @param <K>   key type for saving into state store
 * @param <V>   value type, also for saving into state store
 * @param <VR>  return type
 */
public abstract class ValueTransformerSupplierWithStore<K, V, VR>
        implements ValueTransformerSupplier<V, VR> {

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
    public ValueTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String storeName) {
        StoreBuilder<KeyValueStore<K, V>> store = Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(storeName),
                keyserde,
                valserde)
                .withCachingEnabled();

        builder.addStateStore(store);
        this.storeName = storeName;
        this.transformer = createTransformer();
    }

    /**
     *
     * @return
     */
    protected abstract TransformerImpl createTransformer();

    @Override
    public ValueTransformer<V, VR> get() {
        return transformer;
    }

    /**
     * Implementation of Transformer.
     */
    protected abstract class TransformerImpl implements ValueTransformerWithStore<K, V, VR> {

        /**
         *
         */
        protected KeyValueStore<K, V> stateStore;
        protected ProcessorContext context;

        @Override
        public void init(ProcessorContext pc) {
            this.context = pc;
            this.stateStore = (KeyValueStore<K, V>) pc.getStateStore(storeName);
        }

        @Override
        public abstract VR transform(V current);

        /**
         *
         * @param previous
         * @param current
         * @return
         */
        @Override
        public abstract VR transform(V previous, V current);

        @Override
        @SuppressWarnings("deprecation")
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
