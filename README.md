# Description

A few utilities I found useful for another project to handle
stateful transformations using Kafka streams that create and
add a statestore to streams topology and save the previous value
in the stream so you can compare the current value to it. 
Docs are still work-in-progress and small enhancements will come later.

And maybe the other, bigger project too.

So, if you happen to use these utils, be prepared for changes.

Simple examples of every class can be found in the test directory.

## Main classes

### TransformerSupplierWithStore

Inner class TransformerImpl provides a default implementation for Kafka interface Transformer
that fetches the previous value with the key used from store, calls transform(key, previous, current)
that you must provide yourself, and then saves the old value in the store. Override if you need something
fancier.
 
```
@Override
public VR transform(K key, V current) {
    V previous = stateStore.get(key);
    VR transformed = transform(key, previous, current);
    stateStore.put(key, current);
    return transformed;
}
```

An example of possible usage using a string as the key and fictional classes InputData and TransformedData:

```
    class MyTransformer extends TransformerSupplierWithStore<String, InputData, KeyValue<String, TransformedData>> {

        public MyTransformer(StreamsBuilder builder, Serde<String> keyserde, Serde<InputData> valserde, String storeName) {
            super(builder, keyserde, valserde, storeName);
        }

        @Override
        public TransformerImpl createTransformer() {
            return new TransformerImpl() {
                @Override
                public KeyValue<String, TransformedData> transform(String key, InputData previous, InputData current) {
                    // Or do all the work here in case the transformation is very simple and can be done in a few lines.
                    return transformer(key, previous, current);
                }
 
                private KeyValue<String, TransformedData> transformer(String key, InputData previous, InputData current) {
                    // Do something here to construct a TransformedData transformed and possibly a new key. Remember that previous can be null.
                    return KeyValue.pair(key, transformed);
                }
            };
        }
    }
 
    MyTransformer transformer = new MyTransformer(builder, Serdes.String(), inputSerde, STORE_NAME);
    KStream<tring, InputData> streamin = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), inputSerde));
    KStream<String, TransformedData> streamout = streamin.transform(transformer, STORE_NAME);
```
### ValueTransformerSupplierWithStore

An example of possible usage using a string as the key and fictional classes InputData and TransformedData:

```
    class MyValueTransformer extends ValueTransformerSupplierWithStore<String, InputData, TransformedData> {

        public MyValueTransformer(StreamsBuilder builder, Serde<String> keyserde, Serde<InputData> valserde, String stateStoreName) {
            super(builder, keyserde, valserde, stateStoreName);
        }

        @Override
        public TransformerImpl createTransformer() {
            return new TransformerImpl() {
                @Override
                public TransformedData transform(InputData current) {
                    InputData previous = stateStore.get(current.possiblekey);
                    TransformedData transformed = transform(previous, current);
                    stateStore.put(current.possiblekey, current);
                    return transformed;
                }

                @Override
                public TransformedData transform(InputData previous, InputData current) {
                    // Or do all the work here in case the transformation is very simple and can be done in a few lines.
                    return transformer(previous, current);
                }
                
                private TransformedData transformer(InputData previous, InputData current) {
                    // Do something here to construct a TransformedData transformed. Remember that previous can be null.
                    return transformed.
                }
            };
        }
    }

    MyValueTransformer transformer = new MyValueTransformer(builder, Serdes.String(), inputSerde, STORE_NAME);
    KStream<tring, InputData> streamin = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), inputSerde));
    KStream<String, TransformedData> streamout = streamin.transform(transformer, STORE_NAME);
```

### SimpleTransformerSupplierWithStore

A wrapper for TransformerSupplierWithStore. Use this in case your transformed data has the same key and value types
and you aren't going to change the key. Useful when you only intend to add some missing information to an already
existing value.

Inner class TransformerImpl inherits transform(K key, V current) from TransformerSupplierWithStore.TransformerImpl and
provides a default implementation for transform(K key, V previous, V current) that makes sure yous don't actually change the key.

```
@Override
public KeyValue<K, V> transform(K key, V previous, V current) {
    V transformed = transformValue(previous, current);
    return KeyValue.pair(key, transformed);
}

// Provide your implementation of transformValue(previous, current).
```

### SimpleValueTransformerSupplierWithStore

A wrapper for ValueTransformerSupplierWithStore. Use this in case your transformed data has the same value type.
Useful when you only intend to add some missing information to an already existing value.

## To do:
- Check what withCachingEnabled() actually means. Should it be configurable?
- Consider adding other options too if needed.
- Consider adding other store types too if needed.
