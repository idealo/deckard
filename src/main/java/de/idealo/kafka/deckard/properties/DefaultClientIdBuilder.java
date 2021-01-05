package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultClientIdBuilder implements ClientIdBuilder {

    private final AtomicInteger producerCount = new AtomicInteger(0);

    @Override
    public String buildClientId(KafkaProducer kafkaProducer, Map<String, Object> producerProperties) {
        return producerProperties.get("client.id") + "-deckard-" + producerCount.getAndIncrement() + "-to-" + kafkaProducer.topic();
    }
}
