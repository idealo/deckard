package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.properties.ContextPropertyKafkaProducerPropertiesBuilderConfigurer;
import de.idealo.kafka.deckard.properties.DeckardKafkaPropertiesSupplier;
import de.idealo.kafka.deckard.properties.DefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ContextPropertyKafkaProducerPropertiesBuilderConfigurerAutoConfigurationTest {

    @SpyBean
    private ContextPropertyKafkaProducerPropertiesBuilderConfigurer contextPropertyKafkaProducerPropertiesBuilderConfigurer;

    @Test
    void shouldDefaultToDefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer() {
        assertThat(contextPropertyKafkaProducerPropertiesBuilderConfigurer).isInstanceOf(DefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer.class);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void shouldUseConfiguredContextPropertyKafkaProducerPropertiesBuilderConfigurerForConfiguration() {
        verify(contextPropertyKafkaProducerPropertiesBuilderConfigurer).configureContextPropertyKafkaProducerPropertiesBuilder(any(DeckardKafkaPropertiesSupplier.class));
    }
}
