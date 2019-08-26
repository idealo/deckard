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
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.LongSerializer",
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.IntegerSerializer"
})
@DirtiesContext
public class ProducerBootstrapServersIT {
    private static final String KAFKA_TEST_TOPIC = "my.test.topic";

    @ClassRule
    public static KafkaEmbedded kafkaEmbedded = new KafkaEmbedded(1, true, 1, KAFKA_TEST_TOPIC);

    @Autowired
    private TestConfig.MyProducer myProducer;

    private Consumer<Long, Integer> myConsumer;

    @Before
    public void setUp() {
        final Map<String, Object> consumerProps = consumerProps("testConsumers", "true", kafkaEmbedded);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        final ConsumerFactory<Long, Integer> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        myConsumer = consumerFactory.createConsumer();
        myConsumer.subscribe(Lists.newArrayList(KAFKA_TEST_TOPIC));
    }

    @Test
    public void shouldConfigureAnnotatedProducerWithSerializersViaDefaultFromProperties() {
        myProducer.send(23L, 42);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<Long, Integer> records = myConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).forEach(record -> {
                assertThat(record.key()).isEqualTo(23L);
                assertThat(record.value()).isEqualTo(42);
            });
        });
    }

    @TestConfiguration
    public static class TestConfig {
        @KafkaProducer(topic = KAFKA_TEST_TOPIC)
        interface MyProducer extends GenericProducer<Long, Integer> {
        }
    }
}