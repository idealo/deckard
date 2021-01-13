package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultClientIdBuilderTest {

    private DefaultClientIdBuilder clientIdBuilder;

    @Before
    public void setUp() {
        clientIdBuilder = new DefaultClientIdBuilder();
    }

    @Test
    public void shouldReturnGeneratedClientId() {
        final KafkaProducer producerAnnotation = new TestTopicKafkaProducer("my-topic");
        final Map<String, Object> producerProperties = singletonMap("client.id", "my-client");

        final String clientId = clientIdBuilder.buildClientId(producerAnnotation, producerProperties);

        assertThat(clientId).isEqualTo("my-client-deckard-0-to-my-topic");
    }

    @Test
    public void shouldIterateGeneratedClientIds() {
        final KafkaProducer producerAnnotation = new TestTopicKafkaProducer("my-topic");
        final Map<String, Object> producerProperties = singletonMap("client.id", "my-client");

        rangeClosed(0, 10).forEach(iteration -> {
            final String clientId = clientIdBuilder.buildClientId(producerAnnotation, producerProperties);

            assertThat(clientId).isEqualTo(format("my-client-deckard-%s-to-my-topic", iteration));
        });
    }

    @RequiredArgsConstructor
    static class TestTopicKafkaProducer extends TestKafkaProducer {

        private final String topic;

        @Override
        public String topic() {
            return topic;
        }
    }
}