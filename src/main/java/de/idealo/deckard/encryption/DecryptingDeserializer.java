package de.idealo.deckard.encryption;

import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;

import java.util.Map;

public class DecryptingDeserializer<T> implements Deserializer<T> {

    private final Deserializer<T> embeddedDeserializer;
    private final BytesEncryptor bytesEncryptor;

    public DecryptingDeserializer(String pass, String salt, Deserializer<T> embeddedDeserializer) {
        this.embeddedDeserializer = embeddedDeserializer;
        this.bytesEncryptor = Encryptors.stronger(pass, salt);
    }

    public void configure(Map<String, ?> map, boolean b) {
        embeddedDeserializer.configure(map, b);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        return data != null ? embeddedDeserializer.deserialize(topic, this.bytesEncryptor.decrypt(data)) : null;
    }

    @Override
    public void close() {
        embeddedDeserializer.close();
    }
}
