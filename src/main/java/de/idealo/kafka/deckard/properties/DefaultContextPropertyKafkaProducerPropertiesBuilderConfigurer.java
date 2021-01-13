package de.idealo.kafka.deckard.properties;

import java.util.Map;

import static java.util.Collections.emptyMap;

public class DefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer  implements ContextPropertyKafkaProducerPropertiesBuilderConfigurer {
    @Override
    public ContextPropertyKafkaProducerPropertiesBuilder configureContextPropertyKafkaProducerPropertiesBuilder(DeckardKafkaPropertiesSupplier deckardKafkaPropertiesSupplier) {
        return kafkaProducer -> {
            final Map<String, DeckardKafkaProperties> deckardKafkaPropertiesMap = deckardKafkaPropertiesSupplier.get();
            if (deckardKafkaPropertiesMap.containsKey(kafkaProducer.id())) {
                return deckardKafkaPropertiesMap.get(kafkaProducer.id()).buildProducerProperties();
            }
            return emptyMap();
        };
    }
}
