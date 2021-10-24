package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.properties.ClientIdBuilder;
import de.idealo.kafka.deckard.properties.PassThroughClientIdBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "deckard.features.auto-generate-client-id.enabled=false")
class PassThruClientIdBuilderAutoConfigurationTest {

    @Autowired
    private ClientIdBuilder clientIdBuilder;
    @Autowired
    private DeckardPropertiesAutoConfiguration configuration;

    @Test
    void shouldDisableDefaultClientIdBuilderWhenConfiguredAsDisabled() {
        assertThat(configuration.getFeatures().getAutoGenerateClientId().isEnabled()).isFalse();
    }

    @Test
    void shouldDefaultToDefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer() {
        assertThat(clientIdBuilder).isInstanceOf(PassThroughClientIdBuilder.class);
    }
}
