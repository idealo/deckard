package de.idealo.kafka.deckard.proxy;

import de.idealo.kafka.deckard.encryption.EncryptingSerializer;
import de.idealo.kafka.deckard.producer.GenericProducer;
import de.idealo.kafka.deckard.producer.Producer;
import de.idealo.kafka.deckard.properties.ProducerPropertiesResolver;
import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.BeanExpressionException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static de.idealo.kafka.deckard.util.CaseUtil.splitCamelCase;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
public class ProducerProxyBeanFactory {

    public static final String DEFAULT_FACTORY_BEAN_NAME = "producerProxyBeanFactory";

    private static final Predicate<String> NOT_RESERVED = word -> !word.equalsIgnoreCase("Producer");

    private final ProducerPropertiesResolver producerPropertiesResolver;
    private final ConfigurableBeanFactory configurableBeanFactory;
    private final ApplicationContext applicationContext;

    public ProducerProxyBeanFactory(
            ProducerPropertiesResolver producerPropertiesResolver,
            ConfigurableBeanFactory configurableBeanFactory,
            ApplicationContext applicationContext
    ) {
        this.producerPropertiesResolver = producerPropertiesResolver;
        this.configurableBeanFactory = configurableBeanFactory;
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    public <K, V, T extends GenericProducer<K, V>> T createBean(ClassLoader classLoader, Class<T> producerClass) throws InstantiationException, IllegalAccessException {
        ProducerDefinition<K, V, T> producerDefinition = new ProducerDefinition<>(producerClass);

        KafkaTemplate<K, V> template = createTemplate(producerDefinition);

        Producer<K, V> producer = new Producer<>(template, producerDefinition.getTopic());

        ProducerInvocationHandler<K, V> handler = new ProducerInvocationHandler<>(producer);
        return (T) Proxy.newProxyInstance(classLoader, new Class[]{producerClass}, handler);
    }

    private <K, V, T extends GenericProducer<K, V>> KafkaTemplate<K, V> createTemplate(ProducerDefinition<K, V, T> producerDefinition) {
        final DefaultKafkaProducerFactory<K, V> producerFactory = new DefaultKafkaProducerFactory<>(
                producerDefinition.getProducerProperties(),
                producerDefinition.getKeySerializer(),
                producerDefinition.getValueSerializer()
        );

        return new KafkaTemplate<>(producerFactory);
    }

    @Value
    private class ProducerDefinition<K, V, T extends GenericProducer<K, V>> {

        String topic;
        Serializer<K> keySerializer;
        Serializer<V> valueSerializer;
        Map<String, Object> producerProperties;

        ProducerDefinition(final Class<T> producerClass) throws IllegalAccessException, InstantiationException {
            final KafkaProducer kafkaProducer = producerClass.getAnnotation(KafkaProducer.class);

            this.topic = retrieveTopic(producerClass, kafkaProducer);
            this.producerProperties = producerPropertiesResolver.buildProducerProperties(kafkaProducer);

            this.keySerializer = retrieveKeySerializerBean(kafkaProducer)
                    .orElse(createKeySerializerBean(kafkaProducer, producerProperties));
            keySerializer.configure(producerProperties, true);

            this.valueSerializer = encryptedIfConfigured(kafkaProducer, retrieveValueSerializerBean(kafkaProducer)
                    .orElse(createValueSerializerBean(kafkaProducer, producerProperties)));
            valueSerializer.configure(producerProperties, false);
        }

        private Serializer<V> encryptedIfConfigured(KafkaProducer kafkaProducer, Serializer<V> embeddedSerializer) {
            if (!kafkaProducer.encryptionPassword().isEmpty() || !kafkaProducer.encryptionSalt().isEmpty()) {
                Assert.isTrue(isValidEncryptionSetup(kafkaProducer.encryptionPassword(), kafkaProducer.encryptionSalt()),
                        "Both password and salt have to be set.");
                EmbeddedValueResolver embeddedValueResolver = new EmbeddedValueResolver(configurableBeanFactory);
                String pass = kafkaProducer.encryptionPassword().startsWith("${") && kafkaProducer.encryptionPassword().endsWith("}") ?
                        embeddedValueResolver.resolveStringValue(kafkaProducer.encryptionPassword()) : kafkaProducer.encryptionPassword();
                String salt = kafkaProducer.encryptionSalt().startsWith("${") && kafkaProducer.encryptionSalt().endsWith("}") ?
                        embeddedValueResolver.resolveStringValue(kafkaProducer.encryptionSalt()) : kafkaProducer.encryptionSalt();
                return new EncryptingSerializer<>(pass, salt, embeddedSerializer);
            }
            return embeddedSerializer;
        }

        private boolean isValidEncryptionSetup(String password, String salt) {
            return !password.isEmpty() && !salt.isEmpty();
        }

        private Serializer<V> createValueSerializerBean(KafkaProducer kafkaProducer, Map<String, Object> producerProperties) throws InstantiationException, IllegalAccessException {
            return (Serializer<V>) retrieveValueSerializerClass(kafkaProducer)
                    .orElse((Class) producerProperties.get("value.serializer")).newInstance();
        }

        private Serializer<K> createKeySerializerBean(KafkaProducer kafkaProducer, Map<String, Object> producerProperties) throws InstantiationException, IllegalAccessException {
            return (Serializer<K>) retrieveKeySerializerClass(kafkaProducer)
                    .orElse((Class) producerProperties.get("key.serializer")).newInstance();
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

        private boolean isValueSerializerDefined(final KafkaProducer kafkaProducer) {
            return nonNull(kafkaProducer) && !kafkaProducer.valueSerializer().equals(KafkaProducer.DefaultSerializer.class);
        }

        private boolean isValueSerializerBeanDefined(final KafkaProducer kafkaProducer) {
            return nonNull(kafkaProducer) && !kafkaProducer.valueSerializerBean().isEmpty();
        }

        private boolean keySerializerDefined(final KafkaProducer kafkaProducer) {
            return nonNull(kafkaProducer) && !kafkaProducer.keySerializer().equals(KafkaProducer.DefaultSerializer.class);
        }

        private boolean isKeySerializerBeanDefined(final KafkaProducer kafkaProducer) {
            return nonNull(kafkaProducer) && !kafkaProducer.keySerializerBean().isEmpty();
        }
    }
}
