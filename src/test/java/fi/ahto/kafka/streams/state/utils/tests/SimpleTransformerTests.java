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
package fi.ahto.kafka.streams.state.utils.tests;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fi.ahto.kafka.streams.state.utils.SimpleTransformerSupplierWithStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author Jouni Ahto
 *
 * Inspired by and further modified from the work done by Elliot Kennedy in
 * org.springframework.kafka.kstream.KafkaStreamsJsonSerializationTests.
 */
@RunWith(SpringRunner.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {
    SimpleTransformerTests.INPUT_TOPIC,
    SimpleTransformerTests.TRANSFORMED_TOPIC,})

public class SimpleTransformerTests {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleTransformerTests.class);

    public static final String INPUT_TOPIC = "input-topic";
    public static final String TRANSFORMED_TOPIC = "transformed-topic";

    @Autowired
    private KafkaEmbedded embeddedKafka;

    @Autowired
    private KafkaTemplate<String, CommonData> inputKafkaTemplate;

    @Autowired
    KStream<String, CommonData> kStream;

    @Autowired
    private JsonSerde<CommonData> commonSerde;

    static class CommonData {

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CommonData other = (CommonData) obj;
            if (!Objects.equals(this.VehicleId, other.VehicleId)) {
                return false;
            }
            if (!Objects.equals(this.RecordTime, other.RecordTime)) {
                return false;
            }
            if (!Objects.equals(this.Delay, other.Delay)) {
                return false;
            }
            if (!Objects.equals(this.DelayChange, other.DelayChange)) {
                return false;
            }
            if (!Objects.equals(this.MeasurementLength, other.MeasurementLength)) {
                return false;
            }
            return true;
        }

        public CommonData() {};
        
        public CommonData(String VehicleId, Instant RecordTime, Integer Delay, Integer DelayChange, Integer MeasurementLength) {
            this.VehicleId = VehicleId;
            this.RecordTime = RecordTime;
            this.Delay = Delay;
            this.DelayChange = DelayChange;
            this.MeasurementLength = MeasurementLength;
        }

        public String VehicleId;
        public Instant RecordTime;
        public Integer Delay;
        public Integer DelayChange;
        public Integer MeasurementLength;
    }

    private List<CommonData> Input = new ArrayList<>();
    private List<CommonData> Expected = new ArrayList<>();

    @Before
    public void prepareData() {
        Input.add(new CommonData("123456", Instant.ofEpochSecond(1519557810), 10, null, null));
        Input.add(new CommonData("123456", Instant.ofEpochSecond(1519557830), 40, null, null));
        Input.add(new CommonData("123456", Instant.ofEpochSecond(1519557880), 20, null, null));

        Expected.add(new CommonData("123456", Instant.ofEpochSecond(1519557810), 10, null, null));
        Expected.add(new CommonData("123456", Instant.ofEpochSecond(1519557830), 40, 30, 20));
        Expected.add(new CommonData("123456", Instant.ofEpochSecond(1519557880), 20, -20, 50));
    }

    @Test
    public void testTranformer() throws Exception {
        LOG.debug("Running test");
        Consumer<String, CommonData> consumer = consumer(TRANSFORMED_TOPIC, Serdes.String(), commonSerde);

        // Put some data to the input streamin.
        Input.forEach((payload) -> {
            inputKafkaTemplate.send(INPUT_TOPIC, "21V", payload);
        });
        inputKafkaTemplate.flush();

        // Consume records from the output of the streamin.
        List<CommonData> Results = new ArrayList<>();
        ConsumerRecords<String, CommonData> resultRecords = KafkaTestUtils.getRecords(consumer);
        for (ConsumerRecord<String, CommonData> output : resultRecords) {
            LOG.debug("Received vehicle " + output.value().VehicleId);
            Results.add(output.value());
        }

        int i = 0;
        assertThat(Results,
                containsInAnyOrder(Expected.toArray()));
    }

    // Taken from org.springframework.kafka.kstream.KafkaStreamsJsonSerializationTests
    private <K, V> Consumer<K, V> consumer(String topic, Serde<K> keySerde, Serde<V> valueSerde) throws Exception {
        Map<String, Object> consumerProps
                = KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "false", this.embeddedKafka);
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10000);

        DefaultKafkaConsumerFactory<K, V> kafkaConsumerFactory
                = new DefaultKafkaConsumerFactory<>(consumerProps, keySerde.deserializer(), valueSerde.deserializer());
        Consumer<K, V> consumer = kafkaConsumerFactory.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topic);
        return consumer;
    }

    @Configuration
    @EnableKafka
    @EnableKafkaStreams
    public static class KafkaStreamsConfiguration {

        @Value("${" + KafkaEmbedded.SPRING_EMBEDDED_KAFKA_BROKERS + "}")
        private String brokerAddresses;

        @Bean
        public KafkaTemplate<?, ?> kafkaTemplate() {
            LOG.debug("KafkaTemplate constructed");
            return new KafkaTemplate<>(producerFactory());
        }

        @Autowired
        public JsonSerde<CommonData> trandformedSerde;

        @Bean
        public JsonSerde<CommonData> serdeFactoryTransformedData() {
            LOG.debug("JsonSerde<TransformedData> constructed");
            return new JsonSerde<>(CommonData.class, customizedObjectMapper());
        }

        @Bean
        public ObjectMapper customizedObjectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
            mapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
            mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            LOG.debug("customizedObjectMapper constructed");
            return mapper;
        }

        @Bean
        public ProducerFactory<?, ?> producerFactory() {
            final JsonSerde<CommonData> valueserde = new JsonSerde<>(customizedObjectMapper());
            DefaultKafkaProducerFactory<String, CommonData> factory = new DefaultKafkaProducerFactory<>(producerConfigs());
            factory.setValueSerializer(valueserde.serializer());
            LOG.debug("ProducerFactory constructed");
            return factory;
        }

        @Bean
        public Map<String, Object> producerConfigs() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerAddresses);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            props.put(ProducerConfig.CLIENT_ID_CONFIG, "test-input");
            return props;
        }

        @Bean
        public Map<String, Object> consumerConfigs() {
            Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(this.brokerAddresses, "test-output",
                    "false");
            consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            return consumerProps;
        }

        @Bean
        public ConsumerFactory<String, CommonData> consumerFactory() {
            LOG.debug("ConsumerFactory constructed");
            return new DefaultKafkaConsumerFactory<>(consumerConfigs());
        }

        @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
        public StreamsConfig kStreamsConfigs() {
            Map<String, Object> props = new HashMap<>();
            props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-transformer");
            props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerAddresses);
            props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
            return new StreamsConfig(props);
        }

        @Bean
        public KStream<String, CommonData> kStream(StreamsBuilder builder) {
            final TestTransformer transformer = new TestTransformer(builder, Serdes.String(), trandformedSerde, "test-store");
            final KStream<String, CommonData> streamin = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), trandformedSerde));
            streamin.map((key, value) -> {
                LOG.debug("Received key " + key);
                return KeyValue.pair(key, value);
            });

            LOG.debug("KStream constructed");
            final KStream<String, CommonData> streamout = streamin.transform(transformer, "test-store");
            streamout.to(TRANSFORMED_TOPIC, Produced.with(Serdes.String(), trandformedSerde));
            return streamin;
        }
    }

    static class TestTransformer extends SimpleTransformerSupplierWithStore<String, CommonData> {

        public TestTransformer(StreamsBuilder builder, Serde<String> keyserde, Serde<CommonData> valserde, String stateStoreName) {
            super(builder, keyserde, valserde, stateStoreName);
        }

        @Override
        public TransformerImpl createTransformer() {
            return new TransformerImpl() {
                @Override
                public CommonData transformValue(CommonData v1, CommonData v2) {
                    return transformer(v1, v2);
                }

                // Overriding to get a clean state, otherwise the test will fail.
                @Override
                public void init(ProcessorContext pc) {
                    stateStore = (KeyValueStore<String, CommonData>) pc.getStateStore(storeName);
                    KeyValueIterator<String, CommonData> iter = stateStore.all();
                    while (iter.hasNext()) {
                        KeyValue<String, CommonData> next = iter.next();
                        stateStore.delete(next.key);
                    }
                }
            };
        }

        public CommonData transformer(CommonData v1, CommonData v2) {
            CommonData rval = new CommonData(v2.VehicleId, v2.RecordTime, v2.Delay, null, null);
            // There wasn't any previous value.
            if (v1 == null) {
                return rval;
            }

            if (v1.RecordTime != null && v2.RecordTime != null) {
                rval.MeasurementLength = (int) v2.RecordTime.getEpochSecond() - (int) v1.RecordTime.getEpochSecond();
            }

            if (v1.Delay != null && v2.Delay != null) {
                rval.DelayChange = v2.Delay - v1.Delay;
            }
            return rval;
        }
    }
}
