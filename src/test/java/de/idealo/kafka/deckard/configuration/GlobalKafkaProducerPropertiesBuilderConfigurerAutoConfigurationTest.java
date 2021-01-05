package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.properties.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GlobalKafkaProducerPropertiesBuilderConfigurerAutoConfigurationTest {

    @SpyBean
    private GlobalKafkaProducerPropertiesBuilderConfigurer globalKafkaProducerPropertiesBuilderConfigurer;
    @Test
    public void shouldDefaultToDefaultGlobalKafkaProducerPropertiesBuilderConfigurer() {
        assertThat(globalKafkaProducerPropertiesBuilderConfigurer).isInstanceOf(DefaultGlobalKafkaProducerPropertiesBuilderConfigurer.class);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void shouldUseConfiguredGlobalKafkaProducerPropertiesBuilderConfigurerForConfiguration() {
        verify(globalKafkaProducerPropertiesBuilderConfigurer).configureGlobalKafkaProducerPropertiesBuilder(any(KafkaProperties.class));
    }
}