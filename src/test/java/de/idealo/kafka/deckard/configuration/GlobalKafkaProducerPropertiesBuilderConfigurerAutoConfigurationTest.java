package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.properties.DefaultGlobalKafkaProducerPropertiesBuilderConfigurer;
import de.idealo.kafka.deckard.properties.GlobalKafkaProducerPropertiesBuilderConfigurer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class GlobalKafkaProducerPropertiesBuilderConfigurerAutoConfigurationTest {

    @SpyBean
    private GlobalKafkaProducerPropertiesBuilderConfigurer globalKafkaProducerPropertiesBuilderConfigurer;

    @Test
    void shouldDefaultToDefaultGlobalKafkaProducerPropertiesBuilderConfigurer() {
        assertThat(globalKafkaProducerPropertiesBuilderConfigurer).isInstanceOf(DefaultGlobalKafkaProducerPropertiesBuilderConfigurer.class);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void shouldUseConfiguredGlobalKafkaProducerPropertiesBuilderConfigurerForConfiguration() {
        verify(globalKafkaProducerPropertiesBuilderConfigurer).configureGlobalKafkaProducerPropertiesBuilder(any(KafkaProperties.class));
    }
}
