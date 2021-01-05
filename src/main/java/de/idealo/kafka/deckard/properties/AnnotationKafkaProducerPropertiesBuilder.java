package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;

import java.util.Map;

public interface AnnotationKafkaProducerPropertiesBuilder {

    String DEFAULT_BEAN_NAME = "annotationKafkaProducerPropertiesBuilder";

    Map<String, Object> buildProducerProperties(KafkaProducer kafkaProducer);
}
