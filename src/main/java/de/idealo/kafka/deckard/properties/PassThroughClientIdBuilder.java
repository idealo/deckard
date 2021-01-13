package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;

import java.util.Map;

public class PassThroughClientIdBuilder implements ClientIdBuilder {
    @Override
    public String buildClientId(KafkaProducer kafkaProducer, Map<String, Object> producerProperties) {
        return producerProperties.get("client.id").toString();
    }
}
