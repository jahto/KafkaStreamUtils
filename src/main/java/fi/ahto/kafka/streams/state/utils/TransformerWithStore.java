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
 * @author Jouni Ahto
 * @param <K>   key type, also for saving into state store
 * @param <V>   value type, also for saving into state store
 * @param <VR>  return type
 */
public interface TransformerWithStore<K, V, VR extends KeyValue<?, ?>> extends Transformer<K, V, VR> {

    /**
     *
     * @param key
     * @param previous
     * @param current
     * @return
     */
    VR transform(K key, V previous, V current);
}
