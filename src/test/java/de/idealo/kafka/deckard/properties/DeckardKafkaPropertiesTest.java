package de.idealo.kafka.deckard.properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class DeckardKafkaPropertiesTest {

    @Test
    public void shouldRemoveDefaultKafkaProducerProperties() {
        DeckardKafkaProperties deckardKafkaProperties = new DeckardKafkaProperties();

        assertThat(deckardKafkaProperties.buildProducerProperties()).isEmpty();
    }
}