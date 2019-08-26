package de.idealo.deckard.proxy;

import de.idealo.deckard.producer.GenericProducer;
import de.idealo.deckard.stereotype.KafkaProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
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
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.LongSerializer",
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.IntegerSerializer",
        "deckard.bootstrap-servers=localhost:14242"
})
@DirtiesContext
public class CustomBootstrapServersIT {

    private static final String KAFKA_TEST_TOPIC = "my.test.topic";
    private static final String KAFKA_PROPERTY_TEST_TOPIC = "my.property.test.topic";

    @ClassRule
    public static KafkaEmbedded kafkaEmbedded = new KafkaEmbedded(1, true, 1, KAFKA_TEST_TOPIC, KAFKA_PROPERTY_TEST_TOPIC);

    static {
        kafkaEmbedded.setKafkaPorts(14242);
    }

    @Autowired
    private TestConfig.CustomProducer customProducer;
    @Autowired
    private TestConfig.CustomPropertyProducer customPropertyProducer;

    private Consumer<Long, Integer> customConsumer;

    private Consumer<Long, Integer> customPropertyConsumer;

    @Before
    public void setUp() {
        customConsumer = createConsumer(LongDeserializer.class, IntegerDeserializer.class, KAFKA_TEST_TOPIC, "testConsumers");
        customPropertyConsumer = createConsumer(LongDeserializer.class, IntegerDeserializer.class, KAFKA_PROPERTY_TEST_TOPIC, "testPropertyConsumers");
    }

    @Test
    public void shouldUseBootstrapServersFromAnnotation() {
        customProducer.send(23L, 42);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<Long, Integer> records = customConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).forEach(record -> {
                assertThat(record.key()).isEqualTo(23L);
                assertThat(record.value()).isEqualTo(42);
            });
        });
    }

    @Test
    public void shouldUseBootstrapServersFromResolvedSpelExpression() {
        customPropertyProducer.send(23L, 42);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<Long, Integer> records = customPropertyConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).forEach(record -> {
                assertThat(record.key()).isEqualTo(23L);
                assertThat(record.value()).isEqualTo(42);
            });
        });
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

    @TestConfiguration
    public static class TestConfig {

        @KafkaProducer(topic = KAFKA_TEST_TOPIC, bootstrapServers = "localhost:14242")
        interface CustomProducer extends GenericProducer<Long, Integer> {
        }

        @KafkaProducer(topic = KAFKA_PROPERTY_TEST_TOPIC, bootstrapServers = "${deckard.bootstrap-servers}")
        interface CustomPropertyProducer extends GenericProducer<Long, Integer> {
        }
    }
}