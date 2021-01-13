package de.idealo.kafka.deckard.properties;

import java.util.Map;

public interface GlobalKafkaProducerPropertiesBuilder {

    String DEFAULT_BEAN_NAME = "globalKafkaProducerPropertiesBuilder";

    Map<String, Object> buildProducerProperties();
}
