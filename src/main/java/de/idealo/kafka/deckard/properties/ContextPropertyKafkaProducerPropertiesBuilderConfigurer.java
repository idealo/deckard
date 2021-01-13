package de.idealo.kafka.deckard.properties;

public interface ContextPropertyKafkaProducerPropertiesBuilderConfigurer {

    String DEFAULT_BEAN_NAME = "contextPropertyKafkaProducerPropertiesBuilderConfigurer";

    ContextPropertyKafkaProducerPropertiesBuilder configureContextPropertyKafkaProducerPropertiesBuilder(DeckardKafkaPropertiesSupplier deckardKafkaPropertiesSupplier);
}
