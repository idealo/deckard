package de.idealo.kafka.deckard.proxy;

import de.idealo.kafka.deckard.producer.GenericProducer;
import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.Deserializer;
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
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
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
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "deckard.bootstrap-servers=localhost:14242",
        "deckard.my-pass:mypass",
        "deckard.my-salt:12ab"
})
@DirtiesContext
public class EncryptionIT {

    private static final String KAFKA_TEST_TOPIC = "my.test.topic";
    private static final String KAFKA_TEST_SPEL_TOPIC = "my.test.spel.topic";

    @ClassRule
    public static KafkaEmbedded kafkaEmbedded = new KafkaEmbedded(1, true, 1, KAFKA_TEST_TOPIC, KAFKA_TEST_SPEL_TOPIC);

    static {
        kafkaEmbedded.setKafkaPorts(14242);
    }

    @Autowired
    private TestConfig.EncryptingProducer customProducer;
    @Autowired
    private TestConfig.EncryptingSpelProducer customSpelProducer;

    private Consumer<Long, String> customConsumer;
    private Consumer<Long, String> customSpelConsumer;

    @Before
    public void setUp() {
        customConsumer = createConsumer(LongDeserializer.class, DecryptingStringDeserializer.class, KAFKA_TEST_TOPIC, "testConsumers");
        customSpelConsumer = createConsumer(LongDeserializer.class, DecryptingStringDeserializer.class, KAFKA_TEST_SPEL_TOPIC, "spelConsumers");
    }

    @Test
    public void shouldUseEncryption() {
        customProducer.send(23L, "my-data");

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<Long, String> records = customConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).forEach(record -> {
                assertThat(record.key()).isEqualTo(23L);
                assertThat(record.value()).isEqualTo("my-data");
            });
        });
    }

    @Test
    public void shouldUseEncryptionSetupResolvedSpelExpression() {
        customSpelProducer.send(23L, "my-data");

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<Long, String> records = customSpelConsumer.poll(100);
            assertThat(records).hasSize(1);
            stream(records.spliterator(), false).forEach(record -> {
                assertThat(record.key()).isEqualTo(23L);
                assertThat(record.value()).isEqualTo("my-data");
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

        @KafkaProducer(topic = KAFKA_TEST_TOPIC, encryptionPassword = "mypass", encryptionSalt = "12ab", bootstrapServers = "localhost:14242")
        interface EncryptingProducer extends GenericProducer<Long, String> {
        }

        @KafkaProducer(topic = KAFKA_TEST_SPEL_TOPIC, encryptionPassword = "${deckard.my-pass}", encryptionSalt = "${deckard.my-salt}", bootstrapServers = "localhost:14242")
        interface EncryptingSpelProducer extends GenericProducer<Long, String> {
        }
    }

    public static class DecryptingStringDeserializer implements Deserializer<String> {

        private final BytesEncryptor encryptor = Encryptors.stronger("mypass", "12ab");

        @Override
        public void configure(Map<String, ?> map, boolean b) {
        }

        @Override
        public String deserialize(String s, byte[] bytes) {
            return new String(this.encryptor.decrypt(bytes));
        }

        @Override
        public void close() {

        }
    }
}