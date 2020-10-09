package de.idealo.kafka.deckard.proxy;

import de.idealo.kafka.deckard.producer.GenericProducer;
import de.idealo.kafka.deckard.producer.Producer;
import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.kafka.properties.schema.registry.url=http://my.avro-schema.url",
        "spring.kafka.producer.properties.auto.register.schemas=false"
})
@DirtiesContext
public class ProducerAdditionalPropertiesIT {

    @Autowired
    private CustomKafkaAvroSerializer kafkaAvroSerializer;

    @Test
    public void shouldConfigureGeneralAdditionalProperties() {
        assertThat(kafkaAvroSerializer.autoRegistersSchema()).isFalse();
    }

    @TestConfiguration
    public static class TestConfig {

        @KafkaProducer(topic = "test.topic.additionalProperties", keySerializer = IntegerSerializer.class, valueSerializerBean = "kafkaAvroSerializerForTesting")
        interface AdditionalPropertiesProducer extends GenericProducer<Integer, Long> {
        }

        @Bean("kafkaAvroSerializerForTesting")
        public CustomKafkaAvroSerializer kafkaAvroSerializer() {
            return new CustomKafkaAvroSerializer();
        }
    }

    private static class CustomKafkaAvroSerializer extends KafkaAvroSerializer {
        public CustomKafkaAvroSerializer() {
        }

        public boolean autoRegistersSchema() {
            return this.autoRegisterSchema;
        }
    }

}
