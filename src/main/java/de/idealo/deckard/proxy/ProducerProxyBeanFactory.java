package de.idealo.deckard.proxy;

import de.idealo.deckard.encryption.EncryptingSerializer;
import de.idealo.deckard.producer.GenericProducer;
import de.idealo.deckard.producer.Producer;
import de.idealo.deckard.stereotype.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.BeanExpressionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    public <K, V, T extends GenericProducer<K, V>> T createBean(ClassLoader classLoader, Class<T> producerClass) throws InstantiationException, IllegalAccessException {
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
        producerProps.put("bootstrap.servers", producerDefinition.getBootstrapServers());

        DefaultKafkaProducerFactory<K, V> producerFactory = new DefaultKafkaProducerFactory<>(producerProps, producerDefinition.getKeySerializer(), producerDefinition.getValueSerializer());

        return new KafkaTemplate<>(producerFactory);
    }

    @Value
    private final class ProducerDefinition<K, V, T extends GenericProducer> {

        private final String topic;
        private final Serializer<K> keySerializer;
        private final Serializer<V> valueSerializer;
        private final List<String> bootstrapServers;

        ProducerDefinition(final Class<T> producerClass) throws IllegalAccessException, InstantiationException {
            final KafkaProducer kafkaProducer = producerClass.getAnnotation(KafkaProducer.class);
            this.topic = retrieveTopic(producerClass, kafkaProducer);
            this.keySerializer = retrieveKeySerializerBean(kafkaProducer).orElse(createKeySerializerBean(kafkaProducer));
            this.valueSerializer = encryptedIfConfigured(kafkaProducer, retrieveValueSerializerBean(kafkaProducer).orElse(createValueSerializerBean(kafkaProducer)));
            this.bootstrapServers = retrieveBootstrapServers(kafkaProducer).orElseGet(() -> retrieveDefaultProducerBootstrapServers(kafkaProperties));
        }

        private Serializer<V> encryptedIfConfigured(KafkaProducer kafkaProducer, Serializer<V> embeddedSerializer) {
            if (!kafkaProducer.encryptPassword().equals("") || !kafkaProducer.encryptSalt().equals("")) {
                Assert.isTrue(isValidEncryptionSetup(kafkaProducer.encryptPassword(), kafkaProducer.encryptSalt()),
                        "Both password and salt have to be set.");
                EmbeddedValueResolver embeddedValueResolver = new EmbeddedValueResolver(configurableBeanFactory);
                String pass = kafkaProducer.encryptPassword().startsWith("${") && kafkaProducer.encryptPassword().endsWith("}") ?
                        embeddedValueResolver.resolveStringValue(kafkaProducer.encryptPassword()) : kafkaProducer.encryptPassword();
                String salt = kafkaProducer.encryptSalt().startsWith("${") && kafkaProducer.encryptSalt().endsWith("}") ?
                        embeddedValueResolver.resolveStringValue(kafkaProducer.encryptSalt()) : kafkaProducer.encryptSalt();
                return new EncryptingSerializer<>(pass, salt, embeddedSerializer);
            }
            return embeddedSerializer;
        }

        private boolean isValidEncryptionSetup(String encryptPassword, String encryptSalt) {
            return nonNull(encryptPassword) && nonNull(encryptSalt) && !Objects.equals("", encryptPassword) && !Objects.equals("", encryptSalt);
        }

        private Serializer<V> createValueSerializerBean(KafkaProducer kafkaProducer) throws InstantiationException, IllegalAccessException {
            return (Serializer<V>) retrieveValueSerializerClass(kafkaProducer)
                    .orElse(kafkaProperties.getProducer().getValueSerializer()).newInstance();
        }

        private Serializer<K> createKeySerializerBean(KafkaProducer kafkaProducer) throws InstantiationException, IllegalAccessException {
            return (Serializer<K>) retrieveKeySerializerClass(kafkaProducer)
                    .orElse(kafkaProperties.getProducer().getKeySerializer()).newInstance();
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

        private Optional<Class> retrieveKeySerializerClass(KafkaProducer kafkaProducer) {
            if (keySerializerDefined(kafkaProducer)) {
                return Optional.of(kafkaProducer.keySerializer());
            }
            return Optional.empty();
        }

        private Optional<Class> retrieveValueSerializerClass(KafkaProducer kafkaProducer) {
            if (isValueSerializerDefined(kafkaProducer)) {
                return Optional.of(kafkaProducer.valueSerializer());
            }
            return Optional.empty();
        }

        private Optional<Serializer<V>> retrieveValueSerializerBean(KafkaProducer kafkaProducer) {
            if (isValueSerializerBeanDefined(kafkaProducer)) {
                return Optional.of(kafkaProducer.valueSerializerBean()).map(beanName -> (Serializer<V>) applicationContext.getBean(beanName));
            }
            return Optional.empty();
        }

        private Optional<Serializer<K>> retrieveKeySerializerBean(KafkaProducer kafkaProducer) {
            if (isKeySerializerBeanDefined(kafkaProducer)) {
                return Optional.of(kafkaProducer.keySerializerBean()).map(beanName -> (Serializer<K>) applicationContext.getBean(beanName));
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

        private boolean isValueSerializerBeanDefined(final KafkaProducer kafkaProducer) {
            return nonNull(kafkaProducer) && !kafkaProducer.valueSerializerBean().equals("");
        }

        private boolean keySerializerDefined(final KafkaProducer kafkaProducer) {
            return nonNull(kafkaProducer) && !kafkaProducer.keySerializer().equals(KafkaProducer.DefaultSerializer.class);
        }

        private boolean isKeySerializerBeanDefined(final KafkaProducer kafkaProducer) {
            return nonNull(kafkaProducer) && !kafkaProducer.keySerializerBean().equals("");
        }
    }
}
