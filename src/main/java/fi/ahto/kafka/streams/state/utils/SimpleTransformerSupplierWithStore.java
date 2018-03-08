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
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 *
 * @author Jouni Ahto
 * @param <K>
 * @param <V>
 */
public abstract class SimpleTransformerSupplierWithStore<K, V>
        extends TransformerSupplierWithStore<K, V, K, V> {

    /**
     *
     * @param builder
     * @param keyserde
     * @param valserde
     * @param stateStoreName
     */
    public SimpleTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String stateStoreName) {
        super(builder, keyserde, valserde, valserde, stateStoreName);
    }

    // public static abstract class TransformerImpl<K1, V1, VR1>
    // public static abstract class TransformerImpl
    public abstract static class TransformerImpl<K1, V1, VR1>
            // extends TransformerSupplierWithStore.TransformerImpl
            extends TransformerSupplierWithStore.TransformerImpl<K1, V1, VR1>
            // implements Transformer<K1, V1, KeyValue<K1, V1>>
            // implements Transformer<K1, V1, VR1>
                    {

        public TransformerImpl(String name) {
            super(name);
        }

        @Override
        public VR1 transform(K1 k, V1 v) {
            V1 oldVal = stateStore.get(k);
            V1 newVal = transformValue(oldVal, v);
            stateStore.put(k, newVal);
            return (VR1) KeyValue.pair(k, newVal);
            // TODO: get rid of the cast.
        }

        public abstract V1 transformValue(V1 oldVal, V1 v);

        @Override
        public void init(ProcessorContext pc) {
            super.init(pc);
        }
    }

    // Problems getting this one to compile. I'm not getting something right,
    // at least the syntax of how to do it, if it even can be done. The idea
    // is to provide a default implementation for the simplest and probably
    // most common case where the key doesn't change, and output type of
    // value is the same, it just got transformed in some way.
    // Maybe Java 8 interfaces with default methods is the right solution.
    /*
    public abstract class SimpleTransformerImpl 
            extends TransformerSupplierWithStore.TransformerImpl
            implements Transformer<K, V, KeyValue<K, V>> {
        public KeyValue<K, V> transformValue(K k, V v) {
                    V oldVal = stateStore.get(k);
                    KeyValue<K, V> newVal = transformValue(k, oldVal, v);
                    stateStore.put(k, newVal.value);
                    return newVal;
        }
    }
     */
}
