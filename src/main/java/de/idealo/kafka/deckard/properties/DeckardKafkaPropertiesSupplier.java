package de.idealo.kafka.deckard.properties;

import java.util.Map;
import java.util.function.Supplier;

public interface DeckardKafkaPropertiesSupplier extends Supplier<Map<String, DeckardKafkaProperties>> {
    String DEFAULT_BEAN_NAME = "deckardKafkaPropertiesSupplier";
}
