package de.idealo.kafka.deckard.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultGlobalKafkaProducerPropertiesBuilderConfigurerTest {

    private DefaultGlobalKafkaProducerPropertiesBuilderConfigurer configurer;

    @BeforeEach
    public void setUp() throws Exception {
        configurer = new DefaultGlobalKafkaProducerPropertiesBuilderConfigurer();
    }

    @Test
    void shouldUseProvidedKafkaPropertiesForGeneration() {
        final KafkaProperties globalProperties = new KafkaProperties();
        globalProperties.setClientId("my-client");

        final GlobalKafkaProducerPropertiesBuilder propertiesBuilder = configurer.configureGlobalKafkaProducerPropertiesBuilder(globalProperties);

        assertThat(propertiesBuilder.buildProducerProperties()).isEqualTo(globalProperties.buildProducerProperties());
    }

    @Test
    void shouldUseNewKafkaPropertiesForGenerationWhenMissing() {
        final GlobalKafkaProducerPropertiesBuilder propertiesBuilder = configurer.configureGlobalKafkaProducerPropertiesBuilder(null);

        assertThat(propertiesBuilder.buildProducerProperties()).isEqualTo(new KafkaProperties().buildProducerProperties());
    }
}
