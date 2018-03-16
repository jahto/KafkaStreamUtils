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
```
TransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String storeName)
ValueTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String storeName)
SimpleTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String storeName)
SimpleValueTransformerSupplierWithStore(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String storeName)
```
