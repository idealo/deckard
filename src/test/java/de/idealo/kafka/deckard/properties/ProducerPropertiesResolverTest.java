package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.producer.GenericProducer;
import de.idealo.kafka.deckard.properties.*;
import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class ProducerPropertiesResolverTest {

    private ProducerPropertiesResolver producerPropertiesResolver;
    private ConfigurableBeanFactory factory = new DefaultListableBeanFactory();
    private AnnotationKafkaProducerPropertiesBuilder annotationKafkaProducerPropertiesBuilder;
    private ClientIdBuilder clientIdBuilder;

    private final ContextPropertyKafkaProducerPropertiesBuilder noOpKafkaProducerPropertiesBuilder = (kafkaProducer) -> emptyMap();

    @Before
    public void setUp() {
        factory = new DefaultListableBeanFactory();
        annotationKafkaProducerPropertiesBuilder = new DefaultAnnotationKafkaProducerPropertiesBuilder(new EmbeddedValueResolver(factory));
        clientIdBuilder = new DefaultClientIdBuilder();
    }

    @Test
    public void shouldOverwriteClientIdWhenDefaultCliendIdBuilderIsUsed() {
        final KafkaProducer kafkaProducer = ProducerPropertiesResolverTestProducer.class.getAnnotation(KafkaProducer.class);
        final GlobalKafkaProducerPropertiesBuilder globalKafkaPropertiesSupplier = () -> {
            final KafkaProperties kafkaProperties = new KafkaProperties();
            kafkaProperties.setClientId("myclient");
            return kafkaProperties.buildProducerProperties();
        };
        producerPropertiesResolver = new ProducerPropertiesResolver(globalKafkaPropertiesSupplier, noOpKafkaProducerPropertiesBuilder, annotationKafkaProducerPropertiesBuilder, clientIdBuilder);

        final Map<String, Object> producerProperties = producerPropertiesResolver.buildProducerProperties(kafkaProducer);

        assertThat(producerProperties).containsEntry("client.id", "myclient-deckard-0-to-my-topic");
    }

    @Test
    public void shouldIncrementClientId() {
        final KafkaProducer kafkaProducer = ProducerPropertiesResolverTestProducer.class.getAnnotation(KafkaProducer.class);
        final GlobalKafkaProducerPropertiesBuilder globalKafkaPropertiesSupplier = () -> {
            final KafkaProperties kafkaProperties = new KafkaProperties();
            kafkaProperties.setClientId("myclient");
            return kafkaProperties.buildProducerProperties();
        };
        producerPropertiesResolver = new ProducerPropertiesResolver(globalKafkaPropertiesSupplier, noOpKafkaProducerPropertiesBuilder, annotationKafkaProducerPropertiesBuilder, clientIdBuilder);

        producerPropertiesResolver.buildProducerProperties(kafkaProducer);
        final Map<String, Object> producerProperties = producerPropertiesResolver.buildProducerProperties(kafkaProducer);

        assertThat(producerProperties).containsEntry("client.id", "myclient-deckard-1-to-my-topic");
    }

    @Test
    public void shouldPreferProducerBootstrapServersOverGlobalBootstrapServers() {
        final KafkaProducer kafkaProducer = ProducerPropertiesResolverTestProducer.class.getAnnotation(KafkaProducer.class);
        final List<String> producerBootstrapServers = singletonList("localhost:9093");
        final GlobalKafkaProducerPropertiesBuilder globalKafkaPropertiesSupplier = () -> {
            final KafkaProperties kafkaProperties = new KafkaProperties();
            final List<String> globalBootstrapServers = singletonList("localhost:9092");
            kafkaProperties.setBootstrapServers(globalBootstrapServers);
            kafkaProperties.getProducer().setBootstrapServers(producerBootstrapServers);
            return kafkaProperties.buildProducerProperties();
        };
        producerPropertiesResolver = new ProducerPropertiesResolver(globalKafkaPropertiesSupplier, noOpKafkaProducerPropertiesBuilder, annotationKafkaProducerPropertiesBuilder, clientIdBuilder);

        final Map<String, Object> producerProperties = producerPropertiesResolver.buildProducerProperties(kafkaProducer);

        assertThat(producerProperties).containsEntry("bootstrap.servers", producerBootstrapServers);
    }

    @Test
    public void shouldPreferAnnotationBootstrapServersOverProducerBootstrapServers() {
        final KafkaProducer kafkaProducer = CustomBootstrapServerProducerPropertiesResolverTestProducer.class.getAnnotation(KafkaProducer.class);
        final GlobalKafkaProducerPropertiesBuilder globalKafkaPropertiesSupplier = () -> {
            final KafkaProperties kafkaProperties = new KafkaProperties();
            final List<String> globalBootstrapServers = singletonList("localhost:9092");
            final List<String> producerBootstrapServers = singletonList("localhost:9093");
            kafkaProperties.setBootstrapServers(globalBootstrapServers);
            kafkaProperties.getProducer().setBootstrapServers(producerBootstrapServers);
            return kafkaProperties.buildProducerProperties();
        };
        producerPropertiesResolver = new ProducerPropertiesResolver(globalKafkaPropertiesSupplier, noOpKafkaProducerPropertiesBuilder, annotationKafkaProducerPropertiesBuilder, clientIdBuilder);

        final Map<String, Object> producerProperties = producerPropertiesResolver.buildProducerProperties(kafkaProducer);

        assertThat(producerProperties).containsEntry("bootstrap.servers", asList(kafkaProducer.bootstrapServers()));
    }

    @KafkaProducer(topic = "my-topic")
    private interface ProducerPropertiesResolverTestProducer extends GenericProducer<Long, String>  {

    }

    @KafkaProducer(topic = "my-topic", bootstrapServers = "localhost:9093")
    private interface CustomBootstrapServerProducerPropertiesResolverTestProducer extends GenericProducer<Long, String>  {

    }
}