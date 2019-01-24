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
    public <K, V, T extends GenericProducer<K, V>> T createBean(ClassLoader classLoader, Class<T> clazz) {
        ProducerDefinition producerDefinition = new ProducerDefinition(clazz, kafkaProperties.getProducer().getValueSerializer());

        KafkaTemplate<K, V> template = createTemplate(kafkaProperties, producerDefinition);

        Producer<K, V> producer = new Producer<>(template, producerDefinition.getTopic());

        ProducerInvocationHandler<K, V> handler = new ProducerInvocationHandler<>(producer);
        return (T) Proxy.newProxyInstance(classLoader, new Class[]{clazz}, handler);
    }

    private <K, V> KafkaTemplate<K, V> createTemplate(@Autowired(required = false) KafkaProperties kafkaProperties, ProducerDefinition producerDefinition) {
        KafkaProperties properties = Optional.ofNullable(kafkaProperties).orElseGet(() -> {
            log.warn("You didn't specify any Kafka properties in your configuration. Either this is a test scenario," +
                    "or this was not your intention.");
            return new KafkaProperties();
        });

        Map<String, Object> producerProps = properties.buildProducerProperties();
        producerProps.put("value.serializer", producerDefinition.getValueSerializer());

        DefaultKafkaProducerFactory<K, V> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);

        return new KafkaTemplate<>(producerFactory);
    }

    @Value
    private class ProducerDefinition<T extends GenericProducer> {

        private final String topic;
        private final Class valueSerializer;

        private ProducerDefinition(Class<T> producerClass, Class defaultSerializer) {
            this.topic = retrieveTopic(producerClass);
            this.valueSerializer = retrieveValueSerializer(producerClass, defaultSerializer);
        }

        private String retrieveTopic(Class<T> producerClass) {
            final KafkaProducer kafkaProducer = producerClass.getAnnotation(KafkaProducer.class);
            final String topic;
            if (nonNull(kafkaProducer) && hasText(kafkaProducer.topic())) {
                topic = kafkaProducer.topic();
            } else {
                topic = splitCamelCase(producerClass.getSimpleName()).stream().filter(NOT_RESERVED).collect(joining("."));
            }
            return topic;
        }

        private Class retrieveValueSerializer(Class<T> producerClass, Class serializerFallback) {
            final KafkaProducer kafkaProducer = producerClass.getAnnotation(KafkaProducer.class);
            final Class retrievedSerializer;

            if (nonNull(kafkaProducer) && !kafkaProducer.valueSerializer().equals(KafkaProducer.DefaultSerializer.class)){
                retrievedSerializer = kafkaProducer.valueSerializer();
            } else {
                retrievedSerializer = serializerFallback;
            }

            return retrievedSerializer;
        }
    }
}
