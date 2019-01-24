package de.idealo.deckard.proxy;

import de.idealo.deckard.producer.GenericProducer;
import de.idealo.deckard.producer.Producer;
import de.idealo.deckard.stereotype.KafkaProducer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static de.idealo.deckard.util.CaseUtil.splitCamelCase;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.springframework.util.StringUtils.hasText;

import java.lang.reflect.Proxy;
import java.util.function.Predicate;

import org.springframework.kafka.core.KafkaTemplate;

import de.idealo.deckard.producer.GenericProducer;
import de.idealo.deckard.producer.Producer;
import de.idealo.deckard.stereotype.KafkaProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Slf4j
public class ProxyBeanFactory {

    private static final Predicate<String> NOT_RESERVED = word -> !word.equalsIgnoreCase("Producer");

    private final KafkaTemplate template;
    private final KafkaProperties kafkaProperties;

    @SuppressWarnings("unchecked")
    public <K, V, T extends GenericProducer<K, V>> T createBean(ClassLoader classLoader, Class<T> clazz) {
        ProducerDefinition producerDefinition = new ProducerDefinition(clazz, kafkaProperties.getProducer().getValueSerializer());

        KafkaTemplate<K, V> template = createTemplate(kafkaProperties, producerDefinition);

        Producer<K, V> producer = new Producer<>(template, producerDefinition.getTopic());

        ProducerInvocationHandler<K, V> handler = new ProducerInvocationHandler<>(producer);
        return (T) Proxy.newProxyInstance(classLoader, new Class[]{clazz}, handler);
    }

    private <T> String retrieveTopic(Class<T> clazz) {
        final KafkaProducer kafkaProducer = clazz.getAnnotation(KafkaProducer.class);
        final String topic;
        if (nonNull(kafkaProducer) && hasText(kafkaProducer.topic())) {
            topic = kafkaProducer.topic();
        } else {
            topic = splitCamelCase(clazz.getSimpleName()).stream().filter(NOT_RESERVED).collect(joining("."));
        }
        return topic;
    }

    private <T> Class retrieveSerializer(Class<T> clazz, Class<T> fallback) {
        final KafkaProducer kafkaProducer = clazz.getAnnotation(KafkaProducer.class);
        final Class retrievedSerializer;

        if (nonNull(kafkaProducer) && !kafkaProducer.serializer().equals(KafkaProducer.DefaultSerializer.class)){
            retrievedSerializer = kafkaProducer.serializer();
        } else {
            retrievedSerializer = fallback;
        }

        return retrievedSerializer;
    }

    <K, V> KafkaTemplate<K, V> createTemplate(@Autowired(required = false) KafkaProperties kafkaProperties, ProducerDefinition producerDefinition) {
        KafkaProperties properties = Optional.ofNullable(kafkaProperties).orElseGet(() -> {
            log.warn("You didn't specify any Kafka properties in your configuration. Either this is a test scenario," +
                    "or this was not your intention.");
            return new KafkaProperties();
        });

        properties.getProducer().setValueSerializer(producerDefinition.getSerializer());
        Map<String, Object> producerProps = properties.buildProducerProperties();

        DefaultKafkaProducerFactory<K, V> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);

        return new KafkaTemplate<>(producerFactory);
    }

    @Data
    private class ProducerDefinition {
        private final String topic;
        private final Class serializer;

        public ProducerDefinition(Class annotatedProducer, Class defaultSerializer) {
            this.topic = retrieveTopic(annotatedProducer);
            this.serializer = retrieveSerializer(annotatedProducer, defaultSerializer);
        }
    }
}
