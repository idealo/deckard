package de.idealo.kafka.deckard.encryption;

import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.encrypt.Encryptors;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EncryptingSerializerTest {

    private static final String PASS = "urdfitvozbunim";
    private static final String SALT = "1234467890abcdef";

    @Spy
    private StringSerializer embeddedSerializer = new StringSerializer();

    @Test
    void shouldConfigureEmbeddedSerializer() {
        EncryptingSerializer<String> stringEncryptingSerializer = new EncryptingSerializer<>(PASS, SALT, embeddedSerializer);
        Map<String, String> map = new HashMap<>();
        boolean b = true;

        stringEncryptingSerializer.configure(map, b);

        verify(embeddedSerializer).configure(map, b);
    }

    @Test
    void shouldCloseEmbeddedSerializer() {
        EncryptingSerializer<String> stringEncryptingSerializer = new EncryptingSerializer<>(PASS, SALT, embeddedSerializer);
        stringEncryptingSerializer.close();

        verify(embeddedSerializer).close();
    }

    @Test
    void shouldEncryptOnSerialize() {
        EncryptingSerializer<String> stringEncryptingSerializer = new EncryptingSerializer<>(PASS, SALT, embeddedSerializer);
        byte[] encryptedData = stringEncryptingSerializer.serialize("my-topic", "my-data");

        byte[] decryptedData = Encryptors.stronger(PASS, SALT).decrypt(encryptedData);

        Assertions.assertThat(decryptedData).isEqualTo("my-data".getBytes());
    }
}
