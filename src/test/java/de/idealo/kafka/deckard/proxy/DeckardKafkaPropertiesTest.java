package de.idealo.kafka.deckard.proxy;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=main-kafka:9092",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.IntegerSerializer",
        "deckard.properties.invoices.producer.key-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "deckard.properties.invoices.bootstrap-servers=invoices-kafka:9092",
        "deckard.properties.orders.producer.key-serializer=org.apache.kafka.common.serialization.LongSerializer"
})
@DirtiesContext
public class DeckardKafkaPropertiesTest {

    @Test
    public void name() {
        Assertions.assertThat("").isNotNull();
    }
}