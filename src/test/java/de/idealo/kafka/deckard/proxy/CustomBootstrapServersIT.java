package de.idealo.kafka.deckard.proxy;

import de.idealo.kafka.deckard.producer.GenericProducer;
import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.Map;

import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.LongSerializer",
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.IntegerSerializer",
        "deckard.bootstrap-servers=localhost:14242"
})
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        controlledShutdown = true,
        topics = {"my.test.topic.custom", "my.property.test.topic.custom"},
        count = 1,
        ports = 14242
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomBootstrapServersIT {

    private static final String KAFKA_TEST_TOPIC = "my.test.topic.custom";
    private static final String KAFKA_PROPERTY_TEST_TOPIC = "my.property.test.topic.custom";

    @Autowired
    private EmbeddedKafkaBroker kafkaEmbedded;

    @Autowired
    private TestConfig.CustomProducer customProducer;
    @Autowired
    private TestConfig.CustomPropertyProducer customPropertyProducer;

    private Consumer<Long, Integer> customConsumer;

    private Consumer<Long, Integer> customPropertyConsumer;

    @BeforeEach
    public void setUp() {
        customConsumer = createConsumer(LongDeserializer.class, IntegerDeserializer.class, KAFKA_TEST_TOPIC, "testConsumers");
        customPropertyConsumer = createConsumer(LongDeserializer.class, IntegerDeserializer.class, KAFKA_PROPERTY_TEST_TOPIC, "testPropertyConsumers");
    }

    @Test
    @Timeout(5)
    void shouldUseBootstrapServersFromAnnotation() {
        customProducer.send(23L, 42);

        ConsumerRecords<Long, Integer> records = customConsumer.poll(100);
        assertThat(records).hasSize(1);
        stream(records.spliterator(), false).forEach(record -> {
            assertThat(record.key()).isEqualTo(23L);
            assertThat(record.value()).isEqualTo(42);
        });
    }

    @Test
    @Timeout(5)
    void shouldUseBootstrapServersFromResolvedSpelExpression() {
        customPropertyProducer.send(23L, 42);

        ConsumerRecords<Long, Integer> records = customPropertyConsumer.poll(Duration.ofMillis(1000));
        assertThat(records).hasSize(1);
        stream(records.spliterator(), false).forEach(record -> {
            assertThat(record.key()).isEqualTo(23L);
            assertThat(record.value()).isEqualTo(42);
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
