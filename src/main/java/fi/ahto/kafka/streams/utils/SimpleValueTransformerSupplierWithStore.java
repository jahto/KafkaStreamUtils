/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.kafka.streams.utils;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;

/**
 *
 * @author Jouni Ahto
 * @param <K>
 * @param <V>
 */
public abstract class SimpleValueTransformerSupplierWithStore<K, V>
        extends ValueTransformerSupplierWithStore<K, V, V>
{
    
    /**
     *
     * @param builder
     * @param serdekey
     * @param serdeval
     * @param stateStoreName
     */
    public SimpleValueTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> serdekey, Serde<V> serdeval, String stateStoreName) {
        super(builder, serdekey, serdeval, serdeval, stateStoreName);
    }
    
}
