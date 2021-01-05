package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.config.EmbeddedValueResolver;

import java.util.Arrays;
import java.util.Map;
import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAnnotationKafkaProducerPropertiesBuilderTest {

    @Mock
    private EmbeddedValueResolver embeddedValueResolver;
    @InjectMocks
    private DefaultAnnotationKafkaProducerPropertiesBuilder producerPropertiesBuilder;

    @Test
    public void shouldReturnUnsetBootstrapServerListWhenUndefined() {
        final KafkaProducer producerAnnotation = new TestBootstrapServerKafkaProducer(new String[]{});

        final Map<String, Object> producerProperties = producerPropertiesBuilder.buildProducerProperties(producerAnnotation);

        assertThat(producerProperties).doesNotContainKey("bootstrap.servers");
    }

    @Test
    public void shouldReturnProvidedBootstrapServerListWhenDefined() {
        final String[] bootstrapServers = {"localhost:14242", "localhost:14243"};
        final KafkaProducer producerAnnotation = new TestBootstrapServerKafkaProducer(bootstrapServers);

        final Map<String, Object> producerProperties = producerPropertiesBuilder.buildProducerProperties(producerAnnotation);

        assertThat(producerProperties).containsEntry("bootstrap.servers", Arrays.asList(bootstrapServers));
    }

    @Test
    public void shouldReturnProvidedSpelResolvedBootstrapServerListWhenDefined() {
        final String[] bootstrapServersSpel = {"${deckard.bootstrap-servers}"};
        final String[] bootstrapServers = {"localhost:14242", "localhost:14243"};
        final KafkaProducer producerAnnotation = new TestBootstrapServerKafkaProducer(bootstrapServersSpel);
        when(embeddedValueResolver.resolveStringValue("${deckard.bootstrap-servers}")).thenReturn(join(",", bootstrapServers));

        final Map<String, Object> producerProperties = producerPropertiesBuilder.buildProducerProperties(producerAnnotation);

        assertThat(producerProperties).containsEntry("bootstrap.servers", Arrays.asList(bootstrapServers));
    }

    @RequiredArgsConstructor
    static class TestBootstrapServerKafkaProducer extends TestKafkaProducer {

        private final String[] bootstrapServers;

        @Override
        public String[] bootstrapServers() {
            return bootstrapServers;
        }
    }
}