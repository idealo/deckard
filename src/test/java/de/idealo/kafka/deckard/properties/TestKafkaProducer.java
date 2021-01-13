package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;

class TestKafkaProducer implements KafkaProducer {

    @Override
    public String id() {
        return null;
    }

    @Override
    public String topic() {
        return null;
    }

    @Override
    public String[] bootstrapServers() {
        return null;
    }

    @Override
    public Class keySerializer() {
        return null;
    }

    @Override
    public Class valueSerializer() {
        return null;
    }

    @Override
    public String keySerializerBean() {
        return null;
    }

    @Override
    public String valueSerializerBean() {
        return null;
    }

    @Override
    public String encryptionPassword() {
        return null;
    }

    @Override
    public String encryptionSalt() {
        return null;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return this.getClass();
    }
}
