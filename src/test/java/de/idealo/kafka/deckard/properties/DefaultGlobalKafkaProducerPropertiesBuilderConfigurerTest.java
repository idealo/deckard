package de.idealo.kafka.deckard.properties;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultGlobalKafkaProducerPropertiesBuilderConfigurerTest {

    private DefaultGlobalKafkaProducerPropertiesBuilderConfigurer configurer;

    @Before
    public void setUp() throws Exception {
        configurer = new DefaultGlobalKafkaProducerPropertiesBuilderConfigurer();
    }

    @Test
    public void shouldUseProvidedKafkaPropertiesForGeneration() {
        final KafkaProperties globalProperties = new KafkaProperties();
        globalProperties.setClientId("my-client");

        final GlobalKafkaProducerPropertiesBuilder propertiesBuilder = configurer.configureGlobalKafkaProducerPropertiesBuilder(globalProperties);

        assertThat(propertiesBuilder.buildProducerProperties()).isEqualTo(globalProperties.buildProducerProperties());
    }

    @Test
    public void shouldUseNewKafkaPropertiesForGenerationWhenMissing() {
        final GlobalKafkaProducerPropertiesBuilder propertiesBuilder = configurer.configureGlobalKafkaProducerPropertiesBuilder(null);

        assertThat(propertiesBuilder.buildProducerProperties()).isEqualTo(new KafkaProperties().buildProducerProperties());
    }
}