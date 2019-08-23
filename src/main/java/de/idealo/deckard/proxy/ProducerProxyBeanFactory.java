package de.idealo.deckard.proxy;

import de.idealo.deckard.producer.GenericProducer;
import de.idealo.deckard.producer.Producer;
import de.idealo.deckard.stereotype.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanExpressionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.idealo.deckard.util.CaseUtil.splitCamelCase;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@RequiredArgsConstructor
public class ProducerProxyBeanFactory {

    public static final String DEFAULT_FACTORY_BEAN_NAME = "producerProxyBeanFactory";
    private static final Predicate<String> NOT_RESERVED = word -> !word.equalsIgnoreCase("Producer");

    private final KafkaProperties kafkaProperties;
    private final ConfigurableBeanFactory configurableBeanFactory;

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
        producerProps.put("bootstrap.servers", producerDefinition.getBootstrapServers());

        DefaultKafkaProducerFactory<K, V> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);

        return new KafkaTemplate<>(producerFactory);
    }

    @Value
    private final class ProducerDefinition<T extends GenericProducer> {

        private final String topic;
        private final Class keySerializer;
        private final Class valueSerializer;
        private final List<String> bootstrapServers;

        ProducerDefinition(final Class<T> producerClass) {
            final KafkaProducer kafkaProducer = producerClass.getAnnotation(KafkaProducer.class);
            this.topic = retrieveTopic(producerClass, kafkaProducer);
            this.keySerializer = retrieveKeySerializer(kafkaProducer).orElse(kafkaProperties.getProducer().getKeySerializer());
            this.valueSerializer = retrieveValueSerializer(kafkaProducer).orElse(kafkaProperties.getProducer().getValueSerializer());
            this.bootstrapServers = retrieveBootstrapServers(kafkaProducer).orElseGet(() -> retrieveDefaultProducerBootstrapServers(kafkaProperties));
        }

        private String retrieveTopic(Class<T> producerClass, final KafkaProducer kafkaProducer) {
            if (isTopicDefined(kafkaProducer)) {
                String topicName = kafkaProducer.topic();

                if (topicName.startsWith("${") && topicName.endsWith("}")) {
                    try {
                        EmbeddedValueResolver embeddedValueResolver = new EmbeddedValueResolver(configurableBeanFactory);
                        return embeddedValueResolver.resolveStringValue(topicName);
                    } catch (BeanExpressionException e) {
                        log.error("Failed to parse expression {}.", topicName, e);
                    }
                }

                return topicName;
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
            if (isValueSerializerDefined(kafkaProducer)) {
                return Optional.of(kafkaProducer.valueSerializer());
            }
            return Optional.empty();
        }

        private Optional<List<String>> retrieveBootstrapServers(KafkaProducer kafkaProducer) {
            if (isBootstrapServersDefined(kafkaProducer)) {
                final List<String> servers = stream(kafkaProducer.bootstrapServers()).flatMap(value -> {
                    String resolvedValue = value;
                    if (value.startsWith("${") && value.endsWith("}")) {
                        try {
                            EmbeddedValueResolver embeddedValueResolver = new EmbeddedValueResolver(configurableBeanFactory);
                            resolvedValue = requireNonNull(embeddedValueResolver.resolveStringValue(value));
                        } catch (BeanExpressionException e) {
                            log.error("Failed to parse expression {}.", value, e);
                        }
                    }
                    return Stream.of(resolvedValue.split(","));
                }).collect(toList());
                return Optional.of(servers);
            }
            return Optional.empty();
        }

        private List<String> retrieveDefaultProducerBootstrapServers(KafkaProperties kafkaProperties) {
            final List<String> producerBootstrapServers = Optional.ofNullable(kafkaProperties.getProducer().getBootstrapServers()).orElse(emptyList());
            final List<String> globalBootstrapServers = kafkaProperties.getBootstrapServers();
            return producerBootstrapServers.isEmpty() ? globalBootstrapServers : producerBootstrapServers;
        }

        private boolean isBootstrapServersDefined(KafkaProducer kafkaProducer) {
            return kafkaProducer.bootstrapServers().length > 0;
        }

        private boolean isValueSerializerDefined(final KafkaProducer kafkaProducer) {
            return nonNull(kafkaProducer) && !kafkaProducer.valueSerializer().equals(KafkaProducer.DefaultSerializer.class);
        }
    }
}
