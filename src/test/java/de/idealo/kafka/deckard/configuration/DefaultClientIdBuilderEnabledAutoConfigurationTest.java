package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.properties.ClientIdBuilder;
import de.idealo.kafka.deckard.properties.DefaultClientIdBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class DefaultClientIdBuilderEnabledAutoConfigurationTest {

    @Autowired
    private ClientIdBuilder clientIdBuilder;
    @Autowired
    private DeckardPropertiesAutoConfiguration configuration;

    @Test
    void shouldEnableDefaultDefaultClientIdBuilderIfNotConfiguredOtherwise() {
        assertThat(configuration.getFeatures().getAutoGenerateClientId().isEnabled()).isTrue();
    }

    @Test
    void shouldDefaultToDefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer() {
        assertThat(clientIdBuilder).isInstanceOf(DefaultClientIdBuilder.class);
    }
}
