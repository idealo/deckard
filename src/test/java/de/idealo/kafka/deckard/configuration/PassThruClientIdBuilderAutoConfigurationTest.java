package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.properties.ClientIdBuilder;
import de.idealo.kafka.deckard.properties.PassThroughClientIdBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "deckard.features.auto-generate-client-id.enabled=false")
public class PassThruClientIdBuilderAutoConfigurationTest {

    @Autowired
    private ClientIdBuilder clientIdBuilder;
    @Autowired
    private DeckardPropertiesAutoConfiguration configuration;

    @Test
    public void shouldDisableDefaultClientIdBuilderWhenConfiguredAsDisabled() {
        assertThat(configuration.getFeatures().getAutoGenerateClientId().isEnabled()).isFalse();
    }

    @Test
    public void shouldDefaultToDefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer() {
        assertThat(clientIdBuilder).isInstanceOf(PassThroughClientIdBuilder.class);
    }
}