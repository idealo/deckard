package de.idealo.kafka.deckard.producer;

import lombok.Data;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.util.Lists;
import org.awaitility.Duration;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;

import java.util.Map;

import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.kafka.test.utils.KafkaTestUtils.*;

public class ProducerTombstoneTest {

    public static final String TEST_TOPIC = "test.topic";

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, false, TEST_TOPIC);

    private Producer<String, MyDto> producer;

    private Consumer<String, MyDto> consumer;

    @Before
    public void setUp() {
        Map<String, Object> producerProps = producerProps(embeddedKafka);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        DefaultKafkaProducerFactory<String, MyDto> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String, MyDto> kafkaTemplate = new KafkaTemplate<>(producerFactory);

        this.producer = new Producer<>(kafkaTemplate, TEST_TOPIC);

        Map<String, Object> consumerProps = consumerProps("testGroup", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ConsumerFactory<String, MyDto> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        this.consumer = consumerFactory.createConsumer();
        this.consumer.subscribe(Lists.newArrayList(TEST_TOPIC));
    }

    @Test
    public void shouldSendEmptyMessage() {

        producer.sendEmpty("someKey");

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, MyDto> records = getRecords(consumer);
            assertThat(records.count()).isEqualTo(1);
            stream(records.spliterator(), false).map(ConsumerRecord::key).forEach(key -> assertThat(key).isEqualTo("someKey"));
        });
    }

    @Data
    public static class MyDto {
        private String myValue;
    }
}