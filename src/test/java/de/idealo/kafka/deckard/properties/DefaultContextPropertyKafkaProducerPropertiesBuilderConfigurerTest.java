package de.idealo.kafka.deckard.properties;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultContextPropertyKafkaProducerPropertiesBuilderConfigurerTest {

    private DefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer configurer;

    @BeforeEach
    public void setUp() throws Exception {
        configurer = new DefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer();
    }

    @Test
    void shouldReturnNoPropertiesfromBuilderWhenNoDeckardKafkaPropertiesAvailable() {
        final DeckardKafkaPropertiesSupplier supplier = Collections::emptyMap;

        final ContextPropertyKafkaProducerPropertiesBuilder builder = configurer.configureContextPropertyKafkaProducerPropertiesBuilder(supplier);

        assertThat(builder.buildProducerProperties(new TestIdKafkaProducer("my-id"))).isEmpty();
    }

    @Test
    void shouldReturnNoPropertiesfromBuilderWhenNoDeckardKafkaPropertiesMatchId() {
        final DeckardKafkaProperties deckardKafkaProperties = new DeckardKafkaProperties();
        deckardKafkaProperties.setClientId("test");
        final DeckardKafkaPropertiesSupplier supplier = () -> singletonMap("another-id", deckardKafkaProperties);

        final ContextPropertyKafkaProducerPropertiesBuilder builder = configurer.configureContextPropertyKafkaProducerPropertiesBuilder(supplier);

        assertThat(builder.buildProducerProperties(new TestIdKafkaProducer("my-id"))).isEmpty();
    }

    @Test
    void shouldReturnPropertiesfromBuilderWhenDeckardKafkaPropertiesMatchId() {
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
