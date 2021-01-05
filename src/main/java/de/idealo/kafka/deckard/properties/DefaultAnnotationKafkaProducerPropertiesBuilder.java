package de.idealo.kafka.deckard.properties;

import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanExpressionException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class DefaultAnnotationKafkaProducerPropertiesBuilder implements AnnotationKafkaProducerPropertiesBuilder {

    private final EmbeddedValueResolver embeddedValueResolver;

    @Override
    public Map<String, Object> buildProducerProperties(KafkaProducer kafkaProducer) {
        final Map<String, Object> producerProperties = new HashMap<>();

        retrieveAnnotationBootstrapServers(kafkaProducer).ifPresent(bootstrapServers -> producerProperties.put("bootstrap.servers", bootstrapServers));

        return producerProperties;
    }

    private Optional<List<String>> retrieveAnnotationBootstrapServers(KafkaProducer kafkaProducer) {
        if (isBootstrapServersDefined(kafkaProducer)) {
            final List<String> servers = stream(kafkaProducer.bootstrapServers()).flatMap(value -> {
                String resolvedValue = value;
                if (value.startsWith("${") && value.endsWith("}")) {
                    try {
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

    private boolean isBootstrapServersDefined(KafkaProducer kafkaProducer) {
        return kafkaProducer.bootstrapServers().length > 0;
    }
}
