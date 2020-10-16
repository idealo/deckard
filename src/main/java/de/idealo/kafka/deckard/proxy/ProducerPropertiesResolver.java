package de.idealo.kafka.deckard.proxy;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanExpressionException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Slf4j
public class ProducerPropertiesResolver {

    public static final String DEFAULT_BEAN_NAME = "producerPropertiesResolver";

    private final KafkaProperties globalKafkaProperties;
    private final ConfigurableBeanFactory configurableBeanFactory;
    private final AtomicInteger producerCount = new AtomicInteger(0);

    public ProducerPropertiesResolver(KafkaProperties globalKafkaProperties, ConfigurableBeanFactory configurableBeanFactory) {
        this.configurableBeanFactory = configurableBeanFactory;
        this.globalKafkaProperties = Optional.ofNullable(globalKafkaProperties).orElseGet(() -> {
            log.warn("You didn't specify any Kafka properties in your configuration. Either this is a test scenario, or this was not your intention.");
            return new KafkaProperties();
        });
    }

    public Map<String, Object> buildProducerProperties(KafkaProducer kafkaProducer) {
        final Map<String, Object> producerProperties = globalKafkaProperties.buildProducerProperties();

        producerProperties.put("bootstrap.servers",  retrieveBootstrapServers(kafkaProducer));
        producerProperties.put("client.id", retrieveClientId(kafkaProducer, producerProperties));

        return producerProperties;
    }

    private String retrieveClientId(KafkaProducer kafkaProducer, Map<String, Object> producerProperties) {
        return producerProperties.get("client.id") + "-deckard-" + producerCount.getAndIncrement() + "-to-" + kafkaProducer.topic();
    }

    private List<String> retrieveBootstrapServers(KafkaProducer kafkaProducer) {
        return retrieveAnnotationBootstrapServers(kafkaProducer)
                .orElseGet(() -> retrieveDefaultProducerBootstrapServers(globalKafkaProperties));
    }
    private Optional<List<String>> retrieveAnnotationBootstrapServers(KafkaProducer kafkaProducer) {
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
}
