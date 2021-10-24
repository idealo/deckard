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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.kafka.test.utils.KafkaTestUtils.getRecords;
import static org.springframework.kafka.test.utils.KafkaTestUtils.producerProps;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        controlledShutdown = false,
        topics = "test.topic"
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProducerTest {

    public static final String TEST_TOPIC = "test.topic";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private Producer<String, MyDto> producer;

    private Consumer<String, MyDto> consumer;

    @BeforeEach
    public void setUp() {
        Map<String, Object> producerProps = producerProps(embeddedKafka);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        DefaultKafkaProducerFactory<String, MyDto> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String, MyDto> kafkaTemplate = new KafkaTemplate<>(producerFactory);

        this.producer = new Producer<>(kafkaTemplate, TEST_TOPIC);

        Map<String, Object> consumerProps = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafka));
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
    @Timeout(5)
    void shouldSendMessage() {
        MyDto value = new MyDto();
        value.setMyValue("foobar");
        producer.send(value);

        ConsumerRecords<String, MyDto> records = getRecords(consumer);
        assertThat(records.count()).isEqualTo(1);
        MyDto expected = new MyDto();
        expected.setMyValue("foobar");
        stream(records.spliterator(), false).map(ConsumerRecord::value).forEach(dto -> assertThat(dto).isEqualTo(expected));
    }

    @Data
    public static class MyDto {
        private String myValue;
    }
}
