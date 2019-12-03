package de.idealo.deckard.encryption;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DecryptingDeserializerTest {

    private static final String PASS = "urdfitvozbunim";
    private static final String SALT = "1234467890abcdef";

    @Spy
    private StringDeserializer embeddedSerializer = new StringDeserializer();

    @Test
    public void shouldConfigureEmbeddedSerializer() {
        DecryptingDeserializer<String> stringDecryptingDeserializer = new DecryptingDeserializer<>(PASS, SALT, embeddedSerializer);
        Map<String, String> map = new HashMap<>();
        boolean b = true;

        stringDecryptingDeserializer.configure(map, b);

        verify(embeddedSerializer).configure(map, b);
    }

    @Test
    public void shouldCloseEmbeddedSerializer() {
        DecryptingDeserializer<String> stringDecryptingDeserializer = new DecryptingDeserializer<>(PASS, SALT, embeddedSerializer);
        stringDecryptingDeserializer.close();

        verify(embeddedSerializer).close();
    }

    @Test
    public void shouldEncryptOnSerialize() {
        EncryptingSerializer<String> stringEncryptingSerializer = new EncryptingSerializer<>(PASS, SALT, new StringSerializer());
        DecryptingDeserializer<String> stringDecryptingDeserializer = new DecryptingDeserializer<>(PASS, SALT, embeddedSerializer);
        byte[] encryptedData = stringEncryptingSerializer.serialize("my-topic", "my-data");

        String decryptedData = stringDecryptingDeserializer.deserialize("", encryptedData);

        Assertions.assertThat(decryptedData).isEqualTo("my-data");
    }
}