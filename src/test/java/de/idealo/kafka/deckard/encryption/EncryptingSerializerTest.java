package de.idealo.kafka.deckard.encryption;

import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.encrypt.Encryptors;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EncryptingSerializerTest {

    private static final String PASS = "urdfitvozbunim";
    private static final String SALT = "1234467890abcdef";

    @Spy
    private StringSerializer embeddedSerializer = new StringSerializer();

    @Test
    public void shouldConfigureEmbeddedSerializer() {
        EncryptingSerializer<String> stringEncryptingSerializer = new EncryptingSerializer<>(PASS, SALT, embeddedSerializer);
        Map<String, String> map = new HashMap<>();
        boolean b = true;

        stringEncryptingSerializer.configure(map, b);

        verify(embeddedSerializer).configure(map, b);
    }

    @Test
    public void shouldCloseEmbeddedSerializer() {
        EncryptingSerializer<String> stringEncryptingSerializer = new EncryptingSerializer<>(PASS, SALT, embeddedSerializer);
        stringEncryptingSerializer.close();

        verify(embeddedSerializer).close();
    }

    @Test
    public void shouldEncryptOnSerialize() {
        EncryptingSerializer<String> stringEncryptingSerializer = new EncryptingSerializer<>(PASS, SALT, embeddedSerializer);
        byte[] encryptedData = stringEncryptingSerializer.serialize("my-topic", "my-data");

        byte[] decryptedData = Encryptors.stronger(PASS, SALT).decrypt(encryptedData);

        Assertions.assertThat(decryptedData).isEqualTo("my-data".getBytes());
    }
}