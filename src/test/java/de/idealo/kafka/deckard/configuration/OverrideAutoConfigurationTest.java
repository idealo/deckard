package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.properties.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OverrideAutoConfigurationTest {

    @Autowired
    private GlobalKafkaProducerPropertiesBuilderConfigurer globalKafkaProducerPropertiesBuilderConfigurer;
    @Autowired
    private ContextPropertyKafkaProducerPropertiesBuilderConfigurer contextPropertyKafkaProducerPropertiesBuilderConfigurer;

    @Test
    public void shouldOverrideGlobalKafkaProducerPropertiesBuilderConfigurer() {
        assertThat(globalKafkaProducerPropertiesBuilderConfigurer).isInstanceOf(TestGlobalKafkaProducerPropertiesBuilderConfigurer.class);
    }

    @Test
    public void shouldOverrideContextPropertyKafkaProducerPropertiesBuilderConfigurer() {
        assertThat(contextPropertyKafkaProducerPropertiesBuilderConfigurer).isInstanceOf(TestContextPropertyKafkaProducerPropertiesBuilderConfigurer.class);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean(GlobalKafkaProducerPropertiesBuilderConfigurer.DEFAULT_BEAN_NAME)
        GlobalKafkaProducerPropertiesBuilderConfigurer globalKafkaProducerPropertiesBuilderConfigurer() {
            return new TestGlobalKafkaProducerPropertiesBuilderConfigurer();
        }

        @Bean(ContextPropertyKafkaProducerPropertiesBuilderConfigurer.DEFAULT_BEAN_NAME)
        ContextPropertyKafkaProducerPropertiesBuilderConfigurer contextPropertyKafkaProducerPropertiesBuilderConfigurer() {
            return new TestContextPropertyKafkaProducerPropertiesBuilderConfigurer();
        }
    }

    static class TestGlobalKafkaProducerPropertiesBuilderConfigurer implements GlobalKafkaProducerPropertiesBuilderConfigurer {

        @Override
        public GlobalKafkaProducerPropertiesBuilder configureGlobalKafkaProducerPropertiesBuilder(KafkaProperties springManagedKafkaProperties) {
            return Collections::emptyMap;
        }
    }

    static class TestContextPropertyKafkaProducerPropertiesBuilderConfigurer implements ContextPropertyKafkaProducerPropertiesBuilderConfigurer {

        @Override
        public ContextPropertyKafkaProducerPropertiesBuilder configureContextPropertyKafkaProducerPropertiesBuilder(DeckardKafkaPropertiesSupplier deckardKafkaPropertiesSupplier) {
            return kafkaProducer -> emptyMap();
        }
    }
}