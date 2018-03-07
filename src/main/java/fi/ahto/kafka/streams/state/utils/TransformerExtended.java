/*
 * Copyright 2018 jah.
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

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;

/**
 *
 * @author jah
 */
public interface TransformerExtended<K, V, VR> extends Transformer<K, V, KeyValue<K, VR>> {

    // KeyValue<K, VR> transform(K k, V v1, VR v2);

    @Override
    @SuppressWarnings("deprecation")
    default KeyValue<K, VR> punctuate(long l) {
        // Not needed and also deprecated. Consider adding support
        // for Punctuator functional interface in the future.
        return null;
    }

    @Override
    default void close() {
        // Note: The store should NOT be closed manually here via `stateStore.close()`!
        // The Kafka Streams API will automatically close stores when necessary.
    }
}
