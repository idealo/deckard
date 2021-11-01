package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class PassThroughClientIdBuilderTest {

    private ClientIdBuilder clientIdBuilder;

    @BeforeEach
    public void setUp() throws Exception {
        clientIdBuilder = new PassThroughClientIdBuilder();
    }

    @Test
    void shouldJustPassThroughOriginalValue() {
        final String anyValue = "some-client";
        final KafkaProducer anyKafkaProducer = new TestKafkaProducer();
        final Map<String, Object> properties = singletonMap("client.id", anyValue);

        final String clientId = clientIdBuilder.buildClientId(anyKafkaProducer, properties);

        assertThat(clientId).isEqualTo(anyValue);
    }
}
