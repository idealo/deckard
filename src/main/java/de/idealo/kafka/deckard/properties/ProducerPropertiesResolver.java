package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ProducerPropertiesResolver {

    public static final String DEFAULT_BEAN_NAME = "producerPropertiesResolver";

    private final GlobalKafkaProducerPropertiesBuilder globalKafkaProducerPropertiesBuilder;
    private final ContextPropertyKafkaProducerPropertiesBuilder contextPropertyKafkaProducerPropertiesBuilder;
    private final AnnotationKafkaProducerPropertiesBuilder annotationKafkaProducerPropertiesBuilder;
    private final ClientIdBuilder clientIdBuilder;

    public ProducerPropertiesResolver(
            GlobalKafkaProducerPropertiesBuilder globalKafkaProducerPropertiesBuilder,
            ContextPropertyKafkaProducerPropertiesBuilder contextPropertyKafkaProducerPropertiesBuilder,
            AnnotationKafkaProducerPropertiesBuilder annotationKafkaProducerPropertiesBuilder,
            ClientIdBuilder clientIdBuilder
    ) {
        this.globalKafkaProducerPropertiesBuilder = globalKafkaProducerPropertiesBuilder;
        this.contextPropertyKafkaProducerPropertiesBuilder = contextPropertyKafkaProducerPropertiesBuilder;
        this.annotationKafkaProducerPropertiesBuilder = annotationKafkaProducerPropertiesBuilder;
        this.clientIdBuilder = clientIdBuilder;
    }

    public Map<String, Object> buildProducerProperties(KafkaProducer kafkaProducer) {
        final Map<String, Object> producerProperties = new HashMap<>();

        producerProperties.putAll(globalKafkaProducerPropertiesBuilder.buildProducerProperties());
        producerProperties.putAll(contextPropertyKafkaProducerPropertiesBuilder.buildProducerProperties(kafkaProducer));
        producerProperties.putAll(annotationKafkaProducerPropertiesBuilder.buildProducerProperties(kafkaProducer));
        producerProperties.put("client.id", clientIdBuilder.buildClientId(kafkaProducer, producerProperties));

        return producerProperties;
    }
}
