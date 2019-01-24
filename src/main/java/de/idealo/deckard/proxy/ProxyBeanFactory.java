package de.idealo.deckard.proxy;

import static de.idealo.deckard.util.CaseUtil.splitCamelCase;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.springframework.util.StringUtils.hasText;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import de.idealo.deckard.producer.GenericProducer;
import de.idealo.deckard.producer.Producer;
import de.idealo.deckard.stereotype.KafkaProducer;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProxyBeanFactory {

    private static final Predicate<String> NOT_RESERVED = word -> !word.equalsIgnoreCase("Producer");

    private final KafkaProperties kafkaProperties;

    @SuppressWarnings("unchecked")
    public <K, V, T extends GenericProducer<K, V>> T createBean(ClassLoader classLoader, Class<T> producerClass) {
        ProducerDefinition producerDefinition = new ProducerDefinition(producerClass);

        KafkaTemplate<K, V> template = createTemplate(kafkaProperties, producerDefinition);

        Producer<K, V> producer = new Producer<>(template, producerDefinition.getTopic());

        ProducerInvocationHandler<K, V> handler = new ProducerInvocationHandler<>(producer);
        return (T) Proxy.newProxyInstance(classLoader, new Class[]{producerClass}, handler);
    }

    private <K, V> KafkaTemplate<K, V> createTemplate(@Autowired(required = false) KafkaProperties kafkaProperties, ProducerDefinition producerDefinition) {
        KafkaProperties properties = Optional.ofNullable(kafkaProperties).orElseGet(() -> {
            log.warn("You didn't specify any Kafka properties in your configuration. Either this is a test scenario," +
                    "or this was not your intention.");
            return new KafkaProperties();
        });

        Map<String, Object> producerProps = properties.buildProducerProperties();
        producerProps.put("key.serializer", producerDefinition.getKeySerializer());
        producerProps.put("value.serializer", producerDefinition.getValueSerializer());

        DefaultKafkaProducerFactory<K, V> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);

        return new KafkaTemplate<>(producerFactory);
    }

    @Value
    private class ProducerDefinition<T extends GenericProducer> {

        private final String topic;
        private final Class keySerializer;
        private final Class valueSerializer;

        private ProducerDefinition(final Class<T> producerClass) {
            final KafkaProducer kafkaProducer = producerClass.getAnnotation(KafkaProducer.class);
            this.topic = retrieveTopic(producerClass, kafkaProducer);
            this.keySerializer = retrieveKeySerializer(kafkaProducer).orElse(kafkaProperties.getProducer().getKeySerializer());
            this.valueSerializer = retrieveValueSerializer(kafkaProducer).orElse(kafkaProperties.getProducer().getValueSerializer());
        }

        private String retrieveTopic(Class<T> producerClass, final KafkaProducer kafkaProducer) {
            if (isTopicDefined(kafkaProducer)) {
                return kafkaProducer.topic();
            }
            return generateTopicNameFromProducerClassName(producerClass);
        }

        private String generateTopicNameFromProducerClassName(final Class<T> producerClass) {
            return splitCamelCase(producerClass.getSimpleName()).stream().filter(NOT_RESERVED).collect(joining("."));
        }

        private boolean isTopicDefined(final KafkaProducer kafkaProducer) {
            return nonNull(kafkaProducer) && hasText(kafkaProducer.topic());
        }

        private Optional<Class> retrieveKeySerializer(KafkaProducer kafkaProducer) {
            if (keySerializerDefined(kafkaProducer)) {
                return Optional.of(kafkaProducer.keySerializer());
            }
            return Optional.empty();
        }

        private boolean keySerializerDefined(final KafkaProducer kafkaProducer) {
            return nonNull(kafkaProducer) && !kafkaProducer.keySerializer().equals(KafkaProducer.DefaultSerializer.class);
        }

        private Optional<Class> retrieveValueSerializer(KafkaProducer kafkaProducer) {
            if (isValueSerializerDefined(kafkaProducer)){
                return Optional.of(kafkaProducer.valueSerializer());
            }
            return Optional.empty();
        }

        private boolean isValueSerializerDefined(final KafkaProducer kafkaProducer) {
            return nonNull(kafkaProducer) && !kafkaProducer.valueSerializer().equals(KafkaProducer.DefaultSerializer.class);
        }
    }
}
