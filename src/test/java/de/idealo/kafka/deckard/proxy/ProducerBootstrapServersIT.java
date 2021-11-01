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
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.LongSerializer",
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.IntegerSerializer"
})
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        controlledShutdown = true,
        topics = "my.test.topic"
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProducerBootstrapServersIT {
    private static final String KAFKA_TEST_TOPIC = "my.test.topic";

    @Autowired
    private EmbeddedKafkaBroker kafkaEmbedded;

    @Autowired
    private TestConfig.MyProducer myProducer;

    private Consumer<Long, Integer> myConsumer;

    @BeforeEach
    public void setUp() {
        final Map<String, Object> consumerProps = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", kafkaEmbedded));
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        final ConsumerFactory<Long, Integer> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        myConsumer = consumerFactory.createConsumer();
        myConsumer.subscribe(Lists.newArrayList(KAFKA_TEST_TOPIC));
    }

    @Test
    @Timeout(5)
    void shouldConfigureAnnotatedProducerWithSerializersViaDefaultFromProperties() {
        myProducer.send(23L, 42);

        ConsumerRecords<Long, Integer> records = myConsumer.poll(Duration.ofMillis(1000));
        assertThat(records).hasSize(1);
        stream(records.spliterator(), false).forEach(record -> {
            assertThat(record.key()).isEqualTo(23L);
            assertThat(record.value()).isEqualTo(42);
        });
    }

    @TestConfiguration
    public static class TestConfig {
        @KafkaProducer(topic = KAFKA_TEST_TOPIC)
        interface MyProducer extends GenericProducer<Long, Integer> {
        }
    }
}
