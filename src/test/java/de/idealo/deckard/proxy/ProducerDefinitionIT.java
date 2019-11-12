package de.idealo.deckard.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.idealo.deckard.producer.GenericProducer;
import de.idealo.deckard.stereotype.KafkaProducer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.*;
import org.awaitility.Duration;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.LongSerializer",
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.IntegerSerializer",
        "spel.test.topic=the.test.topic.from.spel"
})
@DirtiesContext
public class ProducerDefinitionIT {
    private static final String KAFKA_TEST_TOPIC_INTEGER = "the.test.topic.integer";
    private static final String KAFKA_TEST_TOPIC_LONG = "the.test.topic.long";
    private static final String KAFKA_TEST_TOPIC_JSON = "the.test.topic.json";
    private static final String KAFKA_TEST_TOPIC_FROM_SPEL = "the.test.topic.from.spel";
    private static final String KAFKA_TEST_TOPIC_BEAN = "the.test.topic.bean";

    @ClassRule
    public static KafkaEmbedded kafkaEmbedded = new KafkaEmbedded(1, true, 1, KAFKA_TEST_TOPIC_INTEGER, KAFKA_TEST_TOPIC_LONG, KAFKA_TEST_TOPIC_JSON, KAFKA_TEST_TOPIC_FROM_SPEL);

    @Autowired
    private ProducerDefinitionIT.TestConfig.LongProducer longProducer;
    @Autowired
    private ProducerDefinitionIT.TestConfig.JsonProducer jsonProducer;
    @Autowired
    private ProducerDefinitionIT.TestConfig.IntegerProducer intProducer;
    @Autowired
    private ProducerDefinitionIT.TestConfig.SpelProducer spelProducer;
    @Autowired
    private ProducerDefinitionIT.TestConfig.ValueBeanProducer beanProducer;

    private Consumer<Long, Integer> intConsumer;
    private Consumer<Integer, Long> longConsumer;
    private Consumer<TestPayload, TestPayload> jsonConsumer;
    private Consumer<TestPayload, TestPayload> spelConsumer;
    private Consumer<TestKey, TestPayload> beanConsumer;

    @Before
    public void setUp() {
        intConsumer = createConsumer(LongDeserializer.class, IntegerDeserializer.class, KAFKA_TEST_TOPIC_INTEGER, "intConsumers");
        longConsumer = createConsumer(IntegerDeserializer.class, LongDeserializer.class, KAFKA_TEST_TOPIC_LONG, "longConsumers");
        jsonConsumer = createConsumer(JsonDeserializer.class, JsonDeserializer.class, KAFKA_TEST_TOPIC_JSON, "jsonConsumer");
        spelConsumer = createConsumer(JsonDeserializer.class, JsonDeserializer.class, KAFKA_TEST_TOPIC_FROM_SPEL, "spelConsumer");
        beanConsumer = createConsumer(new TypeAgnosticDeserializer<>(TestKey.class), new TypeAgnosticDeserializer<>(TestPayload.class), KAFKA_TEST_TOPIC_BEAN, "beanConsumer");
    }

    @Test
    public void shouldConfigureAnnotatedProducerWithSerializersViaDefaultFromProperties() {
        intProducer.send(23L, 42);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<Long, Integer> records = intConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).forEach(record -> {
                assertThat(record.key()).isEqualTo(23L);
                assertThat(record.value()).isEqualTo(42);
            });
        });
    }

    @Test
    public void shouldConfigureAnnotatedProducerWithSerializersWithDefinedLongSerializer() {
        longProducer.send(24, 12L);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<Integer, Long> records = longConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).forEach(record -> {
                assertThat(record.key()).isEqualTo(24);
                assertThat(record.value()).isEqualTo(12L);
            });
        });
    }

    @Test
    public void shouldConfigureAnnotatedProducerWithSerializerWithDefinedJsonSerializer() {
        TestPayload dto = new TestPayload("Hello.");
        jsonProducer.send(dto);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<TestPayload, TestPayload> records = jsonConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).map(ConsumerRecord::value).forEach(value -> assertThat(value).isEqualTo(dto));
        });
    }

    @Test
    public void shouldConfigureAnnotatedProducerWithTopicFromSpelExpression() {
        TestPayload dto = new TestPayload("Hello.");
        spelProducer.send(dto);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<TestPayload, TestPayload> records = spelConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).map(ConsumerRecord::value).forEach(value -> assertThat(value).isEqualTo(dto));
        });
    }

    @Test
    public void shouldConfigureAnnotatedProducerWithSerializersByBeanName() {
        TestKey key = new TestKey("Hello.Key");
        TestPayload payload = new TestPayload("Hello.Value");
        beanProducer.send(key, payload);

        await().atMost(Duration.TEN_SECONDS).untilAsserted(() -> {
            ConsumerRecords<TestKey, TestPayload> records = beanConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).forEach(record -> {
                assertThat(record.key()).isEqualTo(key);
                assertThat(record.value()).isEqualTo(payload);
            });
        });
    }

    @TestConfiguration
    public static class TestConfig {

        @KafkaProducer(topic = KAFKA_TEST_TOPIC_LONG, keySerializer = IntegerSerializer.class, valueSerializer = LongSerializer.class)
        interface LongProducer extends GenericProducer<Integer, Long> {
        }

        @KafkaProducer(topic = KAFKA_TEST_TOPIC_INTEGER)
        interface IntegerProducer extends GenericProducer<Long, Integer> {
        }

        @KafkaProducer(topic = KAFKA_TEST_TOPIC_JSON, keySerializer = JsonSerializer.class, valueSerializer = JsonSerializer.class)
        interface JsonProducer extends GenericProducer<TestPayload, TestPayload> {
        }

        @KafkaProducer(topic = "${spel.test.topic}", keySerializer = JsonSerializer.class, valueSerializer = JsonSerializer.class)
        interface SpelProducer extends GenericProducer<TestPayload, TestPayload> {
        }

        @KafkaProducer(topic = KAFKA_TEST_TOPIC_BEAN, keySerializerBean = "testDtoKeySerializer", valueSerializerBean = "testDtoValueSerializer")
        interface ValueBeanProducer extends GenericProducer<TestKey, TestPayload> {
        }

        @Bean
        public Serializer<TestKey> testDtoValueSerializer() {
            return new JsonSerializer<>();
        }

        @Bean
        public Serializer<TestPayload> testDtoKeySerializer() {
            return new JsonSerializer<>();
        }
    }

    private <K, V> Consumer<K, V> createConsumer(Class keyDeserializer, Class valueDeserializer, String topic, String group) {
        Map<String, Object> consumerProps = consumerProps(group, "true", kafkaEmbedded);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ConsumerFactory<K, V> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        Consumer<K, V> consumer = consumerFactory.createConsumer();
        consumer.subscribe(singletonList(topic));
        return consumer;
    }

    private <K, V> Consumer<K, V> createConsumer(Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer, String topic, String group) {
        Map<String, Object> consumerProps = consumerProps(group, "true", kafkaEmbedded);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ConsumerFactory<K, V> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps, keyDeserializer, valueDeserializer);

        Consumer<K, V> consumer = consumerFactory.createConsumer();
        consumer.subscribe(singletonList(topic));
        return consumer;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class TestKey {
        private String value;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class TestPayload {
        private String message;
    }

    static class TypeAgnosticDeserializer<T> extends JsonDeserializer<T> {

        private static final String TYPE_ID_HEADER = "__TypeId__";
        private static final String KEY_TYPE_ID_HEADER = "__Key_TypeId__";

        TypeAgnosticDeserializer(Class<T> clazz) {
            super(clazz, new ObjectMapper());
        }

        @Override
        public T deserialize(String topic, Headers headers, byte[] data) {
            headers.remove(TYPE_ID_HEADER);
            headers.remove(KEY_TYPE_ID_HEADER);
            return super.deserialize(topic, headers, data);
        }
    }

}