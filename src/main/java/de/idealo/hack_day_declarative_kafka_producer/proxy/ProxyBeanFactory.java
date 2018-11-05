package de.idealo.hack_day_declarative_kafka_producer.proxy;

import de.idealo.hack_day_declarative_kafka_producer.producer.GenericProducer;
import de.idealo.hack_day_declarative_kafka_producer.producer.Producer;
import de.idealo.hack_day_declarative_kafka_producer.stereotype.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

import java.lang.reflect.Proxy;
import java.util.function.Predicate;

import static de.idealo.hack_day_declarative_kafka_producer.util.CaseUtil.splitCamelCase;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class ProxyBeanFactory {

    private static final Predicate<String> NOT_RESERVED = word -> !word.equalsIgnoreCase("Producer");

    private final KafkaTemplate template;

    @SuppressWarnings("unchecked")
    public <K, V, T extends GenericProducer<K, V>> T createBean(ClassLoader classLoader, Class<T> clazz) {
        String topic = retrieveTopic(clazz);

        Producer<K, V> producer = new Producer<>(template, topic);

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
}
