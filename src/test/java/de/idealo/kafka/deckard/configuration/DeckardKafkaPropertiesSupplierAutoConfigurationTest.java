package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.properties.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DeckardKafkaPropertiesSupplierAutoConfigurationTest {

    @Autowired
    private DeckardKafkaPropertiesSupplier deckardKafkaPropertiesSupplier;
    @Autowired
    private DeckardPropertiesAutoConfiguration kafkaPropertiesAutoConfiguration;

    @Test
    public void shouldUseConfigurationDeckardKafkaPropertiesForDeckardKafkaPropertiesSupplier() {
        assertThat(deckardKafkaPropertiesSupplier.get()).isEqualTo(kafkaPropertiesAutoConfiguration.getProperties());
    }
}