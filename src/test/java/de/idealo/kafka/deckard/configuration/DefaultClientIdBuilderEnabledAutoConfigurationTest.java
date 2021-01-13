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
public class DefaultClientIdBuilderEnabledAutoConfigurationTest {

    @Autowired
    private ClientIdBuilder clientIdBuilder;
    @Autowired
    private DeckardPropertiesAutoConfiguration configuration;

    @Test
    public void shouldEnableDefaultDefaultClientIdBuilderIfNotConfiguredOtherwise() {
        assertThat(configuration.getFeatures().getAutoGenerateClientId().isEnabled()).isTrue();
    }

    @Test
    public void shouldDefaultToDefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer() {
        assertThat(clientIdBuilder).isInstanceOf(DefaultClientIdBuilder.class);
    }
}