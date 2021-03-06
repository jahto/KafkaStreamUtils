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

import org.apache.kafka.streams.kstream.ValueTransformer;

/**
 *
 * @author Jouni Ahto
 * @param <K>   key type for saving into state store
 * @param <V>   value type, also for saving into state store
 * @param <VR>  return type
 */
public interface ValueTransformerWithStore<K, V, VR> extends ValueTransformer<V, VR> {
    /**
     *
     * @param previous
     * @param current
     * @return
     */
    VR transform(final V previous, final V current);
}
