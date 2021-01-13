package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class PassThroughClientIdBuilderTest {

    private ClientIdBuilder clientIdBuilder;

    @Before
    public void setUp() throws Exception {
        clientIdBuilder = new PassThroughClientIdBuilder();
    }

    @Test
    public void shouldJustPassThroughOriginalValue() {
        final String anyValue = "some-client";
        final KafkaProducer anyKafkaProducer = new TestKafkaProducer();
        final Map<String, Object> properties = singletonMap("client.id", anyValue);

        final String clientId = clientIdBuilder.buildClientId(anyKafkaProducer, properties);

        assertThat(clientId).isEqualTo(anyValue);
    }
}