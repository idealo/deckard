package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.properties.DeckardKafkaPropertiesSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class DeckardKafkaPropertiesSupplierAutoConfigurationTest {

    @Autowired
    private DeckardKafkaPropertiesSupplier deckardKafkaPropertiesSupplier;
    @Autowired
    private DeckardPropertiesAutoConfiguration kafkaPropertiesAutoConfiguration;

    @Test
    void shouldUseConfigurationDeckardKafkaPropertiesForDeckardKafkaPropertiesSupplier() {
        assertThat(deckardKafkaPropertiesSupplier.get()).isEqualTo(kafkaPropertiesAutoConfiguration.getProperties());
    }
}
