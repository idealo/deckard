package de.idealo.deckard.proxy;

import de.idealo.deckard.producer.GenericProducer;
import de.idealo.deckard.stereotype.KafkaProducer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.assertj.core.util.Lists;
import org.awaitility.Duration;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

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

    private Consumer<Long, Integer> intConsumer;
    private Consumer<Integer, Long> longConsumer;
    private Consumer<TestDto, TestDto> jsonConsumer;
    private Consumer<TestDto, TestDto> spelConsumer;

    @Before
    public void setUp() throws Exception {
        intConsumer = createConsumer(LongDeserializer.class, IntegerDeserializer.class, KAFKA_TEST_TOPIC_INTEGER, "intConsumers");
        longConsumer = createConsumer(IntegerDeserializer.class, LongDeserializer.class, KAFKA_TEST_TOPIC_LONG, "longConsumers");
        jsonConsumer = createConsumer(JsonDeserializer.class, JsonDeserializer.class, KAFKA_TEST_TOPIC_JSON, "jsonConsumer");
        spelConsumer = createConsumer(JsonDeserializer.class, JsonDeserializer.class, KAFKA_TEST_TOPIC_FROM_SPEL, "spelConsumer");
    }

    @Test
    public void shouldConfigureAnnotatedProducerWithSerializersViaDefaultFromProperties() throws Exception {
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
    public void shouldConfigureAnnotatedProducerWithSerializersWithDefinedLongSerializer() throws Exception {
        longProducer.send( 24, 12L);

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
    public void shouldConfigureAnnotatedProducerWithSerializerWithDefinedJsonSerializer() throws Exception {
        TestDto dto = new TestDto("Hello.");
        jsonProducer.send(dto);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<TestDto, TestDto> records = jsonConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).map(ConsumerRecord::value).forEach(value -> assertThat(value).isEqualTo(dto));
        });
    }

    @Test
    public void shouldConfigureAnnotatedProducerWithTopicFromSpelExpression() {
        TestDto dto = new TestDto("Hello.");
        spelProducer.send(dto);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<TestDto, TestDto> records = spelConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).map(ConsumerRecord::value).forEach(value -> assertThat(value).isEqualTo(dto));
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
        interface JsonProducer extends GenericProducer<TestDto, TestDto> {
        }

        @KafkaProducer(topic = "${spel.test.topic}", keySerializer = JsonSerializer.class, valueSerializer = JsonSerializer.class)
        interface SpelProducer extends GenericProducer<TestDto, TestDto> {
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
        consumer.subscribe(Lists.newArrayList(topic));
        return consumer;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class TestDto {
        private String message;
    }
}