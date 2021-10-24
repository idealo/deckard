package de.idealo.kafka.deckard.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
class DeckardKafkaPropertiesTest {

    @Test
    void shouldRemoveDefaultKafkaProducerProperties() {
        DeckardKafkaProperties deckardKafkaProperties = new DeckardKafkaProperties();

        assertThat(deckardKafkaProperties.buildProducerProperties()).isEmpty();
    }
}
