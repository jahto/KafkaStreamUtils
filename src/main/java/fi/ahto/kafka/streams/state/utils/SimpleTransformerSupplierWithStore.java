/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.kafka.streams.state.utils;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Transformer;

/**
 *
 * @author Jouni Ahto
 * @param <K>
 * @param <V>
 */
public abstract class SimpleTransformerSupplierWithStore<K, V> 
        extends TransformerSupplierWithStore<K, V, V>
{
    
    /**
     *
     * @param builder
     * @param keyserde
     * @param valserde
     * @param stateStoreName
     */
    public SimpleTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String stateStoreName) {
        super(builder, keyserde, valserde, stateStoreName);
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
        public KeyValue<K, V> transform(K k, V v) {
                    V oldVal = stateStore.get(k);
                    KeyValue<K, V> newVal = transform(k, oldVal, v);
                    stateStore.put(k, newVal.value);
                    return newVal;
        }
    }
    */
}
