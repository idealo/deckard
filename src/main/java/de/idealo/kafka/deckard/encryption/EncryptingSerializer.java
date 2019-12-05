package de.idealo.kafka.deckard.encryption;

import org.apache.kafka.common.serialization.Serializer;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;

import java.util.Map;
import java.util.Objects;

public class EncryptingSerializer<T> implements Serializer<T> {

    private final Serializer<T> embeddedSerializer;
    private final BytesEncryptor bytesEncryptor;

    public EncryptingSerializer(String pass, String salt, Serializer<T> embeddedSerializer) {
        this.embeddedSerializer = embeddedSerializer;
        this.bytesEncryptor = Encryptors.stronger(pass, salt);
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {
        embeddedSerializer.configure(map, b);
    }

    @Override
    public byte[] serialize(String topic, T data) {
        return Objects.nonNull(data) ? this.bytesEncryptor.encrypt(embeddedSerializer.serialize(topic, data)) : null;
    }

    @Override
    public void close() {
        embeddedSerializer.close();
    }
}
