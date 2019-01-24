package de.idealo.deckard.proxy;

import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;

import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
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

import de.idealo.deckard.producer.GenericProducer;
import de.idealo.deckard.stereotype.KafkaProducer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}", "spring.kafka.producer.value-serializer: org.apache.kafka.common.serialization.IntegerSerializer"})
@DirtiesContext
public class ProducerDefinitionIT {
    private static final String KAFKA_TEST_TOPIC_INTEGER = "the.test.topic.integer";
    private static final String KAFKA_TEST_TOPIC_LONG = "the.test.topic.long";
    private static final String KAFKA_TEST_TOPIC_JSON = "the.test.topic.json";

    @ClassRule
    public static KafkaEmbedded kafkaEmbedded = new KafkaEmbedded(1, true, 1, KAFKA_TEST_TOPIC_INTEGER, KAFKA_TEST_TOPIC_LONG, KAFKA_TEST_TOPIC_JSON);

    @Autowired
    private ProducerDefinitionIT.TestConfig.LongProducer longProducer;

    @Autowired
    private ProducerDefinitionIT.TestConfig.JsonProducer jsonProducer;

    @Autowired
    private ProducerDefinitionIT.TestConfig.IntegerProducer intProducer;

    private Consumer<String, Integer> intConsumer;
    private Consumer<String, Long> longConsumer;
    private Consumer<String, TestDto> jsonConsumer;

    @Before
    public void setUp() throws Exception {
        intConsumer = createConsumer(IntegerDeserializer.class, KAFKA_TEST_TOPIC_INTEGER, "intConsumers");
        longConsumer = createConsumer(LongDeserializer.class, KAFKA_TEST_TOPIC_LONG, "longConsumers");
        jsonConsumer = createConsumer(JsonDeserializer.class, KAFKA_TEST_TOPIC_JSON, "jsonConsumer");
    }

    @Test
    public void shouldConfigureAnnotatedProducerWithSerializerViaDefaultFromProperties() throws Exception {
        intProducer.send(42);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, Integer> records = intConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).map(ConsumerRecord::value).forEach(value -> assertThat(value).isEqualTo(42));
        });
    }

    @Test
    public void shouldConfigureAnnotatedProducerWithSerializerWithDefinedLongSerializer() throws Exception {
        longProducer.send(12L);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, Long> records = longConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).map(ConsumerRecord::value).forEach(value -> assertThat(value).isEqualTo(12L));
        });
    }

    @Test
    public void shouldConfigureAnnotatedProducerWithSerializerWithDefinedJsonSerializer() throws Exception {
        TestDto dto = new TestDto("Hello.");
        jsonProducer.send(dto);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, TestDto> records = jsonConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).map(ConsumerRecord::value).forEach(value -> assertThat(value).isEqualTo(dto));
        });
    }

    @TestConfiguration
    public static class TestConfig {
        @KafkaProducer(topic = KAFKA_TEST_TOPIC_LONG, valueSerializer = LongSerializer.class)
        interface LongProducer extends GenericProducer<String, Long> {
        }

        @KafkaProducer(topic = KAFKA_TEST_TOPIC_JSON, valueSerializer = JsonSerializer.class)
        interface JsonProducer extends GenericProducer<String, TestDto> {
        }

        @KafkaProducer(topic = KAFKA_TEST_TOPIC_INTEGER)
        interface IntegerProducer extends GenericProducer<String, Integer> {
        }
    }

    private <V> Consumer<String, V> createConsumer(Class valueDeserializer, String topic, String group) {
        Map<String, Object> consumerProps = consumerProps(group, "true", kafkaEmbedded);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ConsumerFactory<String, V> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        Consumer<String, V> consumer = consumerFactory.createConsumer();
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