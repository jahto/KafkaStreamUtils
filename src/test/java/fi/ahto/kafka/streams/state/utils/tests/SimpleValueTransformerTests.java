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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fi.ahto.kafka.streams.state.utils.TransformerSupplierWithStore;
import fi.ahto.kafka.streams.state.utils.ValueTransformerSupplierWithStore;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.StreamsBuilderFactoryBean;
import org.springframework.kafka.support.serializer.JsonSerde;
import static org.hamcrest.Matchers.*;
// import org.hamcrest.collection.*;
// import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;

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
    SimpleValueTransformerTests.INPUT_TOPIC,
    SimpleValueTransformerTests.TRANSFORMED_TOPIC,})

// Not yet modified to use SimpleValueTransformerWithStore...
public class SimpleValueTransformerTests {

    public static final String INPUT_TOPIC = "input-topic";
    public static final String TRANSFORMED_TOPIC = "transformed-topic";

    @Autowired
    private KafkaEmbedded embeddedKafka;

    @Autowired
    private KafkaTemplate<String, InputData> inputKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, TransformedData> transformedKafkaTemplate;

    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    KStream<String, InputData> kStream;

    // NOTE: do NOT use these serdes, for some reason they always end up containing
    // an ObjectMapper that is not customized, although the autowired one above is.
    // Maybe converting them to beans could help, either here or in the configuration class?
    // private final JsonSerde<InputData> inputSerde = new JsonSerde<>(InputData.class, objectMapper);
    // private final JsonSerde<TransformedData> trandformedSerde = new JsonSerde<>(TransformedData.class, objectMapper);
    // These ones compile, but the deserializer is still missing targettype. At least
    // they are using the right ObjectMapper, the one configured in Configuration class.
    // But let's test whether they work anyway. Ok, occasionally, but in some cases throw
    // during deserialization, because now the are not in trusted packages... Solved
    // by adding the type in JsonSerde constructor in beans.
    @Autowired
    private JsonSerde<InputData> inputSerde;
    @Autowired
    private JsonSerde<TransformedData> transformedSerde;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InputData {

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
            final InputData other = (InputData) obj;
            if (!Objects.equals(this.VehicleId, other.VehicleId)) {
                return false;
            }
            if (!Objects.equals(this.RecordTime, other.RecordTime)) {
                return false;
            }
            if (!Objects.equals(this.Delay, other.Delay)) {
                return false;
            }
            return true;
        }

        @JsonProperty("VehicleId")
        public String getVehicleId() {
            return VehicleId;
        }

        @JsonProperty("VehicleId")
        public void setVehicleId(String VehicleId) {
            this.VehicleId = VehicleId;
        }

        @JsonProperty("RecordTime")
        public Instant getRecordTime() {
            return RecordTime;
        }

        @JsonProperty("RecordTime")
        public void setRecordTime(Instant RecordTime) {
            this.RecordTime = RecordTime;
        }

        @JsonProperty("Delay")
        public Integer getDelay() {
            return Delay;
        }

        @JsonProperty("Delay")
        public void setDelay(Integer Delay) {
            this.Delay = Delay;
        }

        public InputData() {};
        
        public InputData(String VehicleId,
                Instant RecordTime,
                Integer Delay) {
            this.VehicleId = VehicleId;
            this.RecordTime = RecordTime;
            this.Delay = Delay;
        }

        @JsonProperty("VehicleId")
        private String VehicleId;
        @JsonProperty("RecordTime")
        private Instant RecordTime;
        @JsonProperty("Delay")
        private Integer Delay;
    }

    static class TransformedData {

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
            final TransformedData other = (TransformedData) obj;
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

        public TransformedData() {};
        
        public TransformedData(String VehicleId, Instant RecordTime, Integer Delay, Integer DelayChange, Integer MeasurementLength) {
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

    private List<InputData> Input = new ArrayList<>();
    private List<TransformedData> Expected = new ArrayList<>();

    @Before
    public void prepareData() {
        Input.add(new InputData("123456", Instant.ofEpochSecond(1519557810), 10));
        Input.add(new InputData("123456", Instant.ofEpochSecond(1519557830), 40));
        Input.add(new InputData("123456", Instant.ofEpochSecond(1519557880), 20));

        Expected.add(new TransformedData("123456", Instant.ofEpochSecond(1519557810), 10, null, null));
        Expected.add(new TransformedData("123456", Instant.ofEpochSecond(1519557830), 40, 30, 20));
        Expected.add(new TransformedData("123456", Instant.ofEpochSecond(1519557880), 20, -20, 50));
    }

    @Test
    public void testTranformer() throws Exception {
        System.out.println("Running test");
        Consumer<String, TransformedData> consumer = consumer(TRANSFORMED_TOPIC, Serdes.String(), transformedSerde);

        // Put some data to the input streamin.
        Input.forEach((payload) -> {
            inputKafkaTemplate.send(INPUT_TOPIC, "21V", payload);
        });
        inputKafkaTemplate.flush();

        // Consume records from the output of the streamin.
        List<TransformedData> Results = new ArrayList<>();
        ConsumerRecords<String, TransformedData> resultRecords = KafkaTestUtils.getRecords(consumer);
        for (ConsumerRecord<String, TransformedData> output : resultRecords) {
            System.out.println("Received vehicle " + output.value().VehicleId);
            Results.add(output.value());
        }

        int i = 0;
        // assertThat(Results,
        //        containsInAnyOrder(Expected));
    }

    // Taken from ???
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
            System.out.println("KafkaTemplate constructed");
            return new KafkaTemplate<>(producerFactory());
        }

        @Autowired
        public JsonSerde<InputData> inputSerde;
        @Autowired
        public JsonSerde<TransformedData> trandformedSerde;

        @Bean
        public JsonSerde<InputData> serdeFactoryInputData() {
            System.out.println("JsonSerde<InputData> constructed");
            return new JsonSerde<>(InputData.class, customizedObjectMapper());
        }

        @Bean
        public JsonSerde<TransformedData> serdeFactoryTransformedData() {
            System.out.println("JsonSerde<TransformedData> constructed");
            return new JsonSerde<>(TransformedData.class, customizedObjectMapper());
        }

        @Bean
        public ObjectMapper customizedObjectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
            mapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
            mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            System.out.println("customizedObjectMapper constructed");
            return mapper;
        }

        @Bean
        public ProducerFactory<?, ?> producerFactory() {
            final JsonSerde<InputData> valueserde = new JsonSerde<>(customizedObjectMapper());
            DefaultKafkaProducerFactory<String, InputData> factory = new DefaultKafkaProducerFactory<>(producerConfigs());
            factory.setValueSerializer(valueserde.serializer());
            System.out.println("ProducerFactory constructed");
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
            Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(this.brokerAddresses, "testGroup",
                    "false");
            consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            return consumerProps;
        }

        @Bean
        public ConsumerFactory<String, InputData> consumerFactory() {
            System.out.println("ConsumerFactory constructed");
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
        public KStream<String, InputData> kStream(StreamsBuilder builder) {
            final TestValueTransformer transformer = new TestValueTransformer(builder, Serdes.String(), inputSerde, "test-store");
            // transformer.cleanStore();
            final KStream<String, InputData> streamin = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), inputSerde));
            streamin.map((key, value) -> {
                System.out.println("Received key " + key);
                return KeyValue.pair(key, value);
            });

            System.out.println("KStream constructed");
            final KStream<String, TransformedData> streamout = streamin.transformValues(transformer, "test-store");
            streamout.to(TRANSFORMED_TOPIC, Produced.with(Serdes.String(), trandformedSerde));
            return streamin;
        }
    }
    
    static class TestValueTransformer extends ValueTransformerSupplierWithStore<String, InputData, TransformedData> {

        public TestValueTransformer(StreamsBuilder builder, Serde<String> keyserde, Serde<InputData> valserde, String stateStoreName) {
            super(builder, keyserde, valserde, stateStoreName);
        }

        @Override
        public TransformerImpl createTransformer() {
            return new TransformerImpl() {
                @Override
                public TransformedData transform(InputData v) {
                    InputData val = stateStore.get(v.getVehicleId());
                    TransformedData newVal = transform(val, v);
                    stateStore.put(v.getVehicleId(), v);
                    return newVal;
                }

                @Override
                public TransformedData transform(InputData v1, InputData v2) {
                    return transformer(v1, v2);
                }
            };
        }
        
        public TransformedData transformer(InputData v1, InputData v2) {
            TransformedData rval = new TransformedData(v2.VehicleId, v2.RecordTime, v2.Delay, null, null);
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
