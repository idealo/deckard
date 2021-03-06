package de.idealo.kafka.deckard.proxy;

import de.idealo.kafka.deckard.producer.GenericProducer;
import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.assertj.core.util.Lists;
import org.awaitility.Duration;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;
import static org.springframework.kafka.test.utils.KafkaTestUtils.getRecords;


@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class ProducerProxyBeanFactoryIT {

    private static final String KAFKA_TEST_TOPIC = "the.test.topic";

    @ClassRule
    public static KafkaEmbedded kafkaEmbedded = new KafkaEmbedded(1, true, KAFKA_TEST_TOPIC);

    @Autowired
    TestProducer producer;

    @Test
    public void shouldConfigureAnnotatedTopic() throws Exception {

        Map<String, Object> consumerProps = consumerProps("testGroup", "true", kafkaEmbedded);
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaEmbedded.getBrokersAsString());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ConsumerFactory<String, Integer> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        Consumer<String, Integer> consumer = consumerFactory.createConsumer();
        consumer.subscribe(Lists.newArrayList(KAFKA_TEST_TOPIC));

        producer.send(42);

        await().atMost(Duration.FIVE_SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, Integer> records = getRecords(consumer);
            assertThat(records.count()).isEqualTo(1);
            stream(records.spliterator(), false).map(ConsumerRecord::value).forEach(value -> assertThat(value).isEqualTo(42));
        });
    }

    @KafkaProducer(topic = KAFKA_TEST_TOPIC, valueSerializer = IntegerSerializer.class)
    interface TestProducer extends GenericProducer<String, Integer> {
    }
}