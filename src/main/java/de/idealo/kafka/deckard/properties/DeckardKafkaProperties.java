package de.idealo.kafka.deckard.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

@Slf4j
@Data
public class DeckardKafkaProperties extends KafkaProperties {

    public DeckardKafkaProperties() {
        super();
        this.setBootstrapServers(null);

        this.getJaas().setLoginModule(null);
        this.getJaas().setControlFlag(null);
        this.getJaas().setOptions(null);

        this.getListener().setType(null);

        this.getProducer().setKeySerializer(null);
        this.getProducer().setValueSerializer(null);

        this.getConsumer().setKeyDeserializer(null);
        this.getConsumer().setValueDeserializer(null);
    }
}
