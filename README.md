# Description

A few utilities I found useful for another project to handle
stateful transformations using Kafka streams that create and
add a statestore to streams topology and save the previous value
in the stream so you can compare the current value to it. 
Docs are still work-in-progress and small enhancements will come later.

And maybe the other, bigger project too.

So, if you happen to use these utils, be prepared for changes.

Simple examples of every class can found in the tests.

## Main classes

### TransformerSupplierWithStore

Inner class TransformerImpl provides a default implementation for Kafka interface Transformer
that fetches the previous value with the key used from store, calls transform(key, oldVal, value)
that you must provide yourself, and then saves the old value in the store. Override if you need something
fancier.
 
```
       @Override
       public VR transform(K key, V value) {
           V oldVal = stateStore.get(key);
           VR newVal = transform(key, oldVal, value);
           stateStore.put(key, value);
           return newVal;
       }
```
```
TransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String storeName)
```
### ValueTransformerSupplierWithStore
```
ValueTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String storeName)
```
### SimpleTransformerSupplierWithStore
A wrapper for TransformerSupplierWithStore. Use this in case your transformed data has the same key and value types
and you aren't going to change the key. Useful when you only intend to add some missing information to an already
existing value. 
```
SimpleTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String storeName)
```
### SimpleValueTransformerSupplierWithStore
A wrapper for ValueTransformerSupplierWithStore. Use this in case transformed data has the same value.
Useful when you only intend to add some missing information to an already existing value.
```
SimpleValueTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String storeName)
```
