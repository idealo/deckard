package de.idealo.kafka.deckard.properties;

import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultContextPropertyKafkaProducerPropertiesBuilderConfigurerTest {

    private DefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer configurer;

    @Before
    public void setUp() throws Exception {
        configurer = new DefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer();
    }

    @Test
    public void shouldReturnNoPropertiesfromBuilderWhenNoDeckardKafkaPropertiesAvailable() {
        final DeckardKafkaPropertiesSupplier supplier = Collections::emptyMap;

        final ContextPropertyKafkaProducerPropertiesBuilder builder = configurer.configureContextPropertyKafkaProducerPropertiesBuilder(supplier);

        assertThat(builder.buildProducerProperties(new TestIdKafkaProducer("my-id"))).isEmpty();
    }

    @Test
    public void shouldReturnNoPropertiesfromBuilderWhenNoDeckardKafkaPropertiesMatchId() {
        final DeckardKafkaProperties deckardKafkaProperties = new DeckardKafkaProperties();
        deckardKafkaProperties.setClientId("test");
        final DeckardKafkaPropertiesSupplier supplier = () -> singletonMap("another-id", deckardKafkaProperties);

        final ContextPropertyKafkaProducerPropertiesBuilder builder = configurer.configureContextPropertyKafkaProducerPropertiesBuilder(supplier);

        assertThat(builder.buildProducerProperties(new TestIdKafkaProducer("my-id"))).isEmpty();
    }

    @Test
    public void shouldReturnPropertiesfromBuilderWhenDeckardKafkaPropertiesMatchId() {
        final DeckardKafkaProperties deckardKafkaProperties = new DeckardKafkaProperties();
        deckardKafkaProperties.setClientId("test");
        final DeckardKafkaPropertiesSupplier supplier = () -> singletonMap("my-id", deckardKafkaProperties);

        final ContextPropertyKafkaProducerPropertiesBuilder builder = configurer.configureContextPropertyKafkaProducerPropertiesBuilder(supplier);

        assertThat(builder.buildProducerProperties(new TestIdKafkaProducer("my-id"))).isEqualTo(deckardKafkaProperties.buildProducerProperties());
    }

    @RequiredArgsConstructor
    static class TestIdKafkaProducer extends TestKafkaProducer {

        private final String id;

        @Override
        public String id() {
            return id;
        }
    }
}