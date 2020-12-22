package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.properties.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;

@Data
@Slf4j
@AutoConfigureAfter({KafkaAutoConfiguration.class})
@ConfigurationProperties(prefix = "deckard")
@Configuration
public class KafkaPropertiesAutoConfiguration {

    private Map<String, DeckardKafkaProperties> properties = emptyMap();

    @Bean(DeckardKafkaPropertiesSupplier.DEFAULT_BEAN_NAME)
    @ConditionalOnMissingBean(DeckardKafkaPropertiesSupplier.class)
    public DeckardKafkaPropertiesSupplier deckardKafkaPropertiesSupplier() {
        return () -> properties;
    }

    @Bean(GlobalKafkaPropertiesSupplierConfigurer.DEFAULT_BEAN_NAME)
    @ConditionalOnMissingBean(GlobalKafkaPropertiesSupplierConfigurer.class)
    GlobalKafkaPropertiesSupplierConfigurer configureGlobalKafkaPropertiesSupplier() {
        return springManagedKafkaProperties -> () -> {
            if (isNull(springManagedKafkaProperties)) {
                log.warn("You didn't specify any Kafka properties in your configuration. Either this is a test scenario, or this was not your intention.");
                return new KafkaProperties();
            }
            return springManagedKafkaProperties;
        };
    }

    @Bean(GlobalKafkaPropertiesSupplier.DEFAULT_BEAN_NAME)
    GlobalKafkaPropertiesSupplier globalKafkaPropertiesSupplier(GlobalKafkaPropertiesSupplierConfigurer configurer, KafkaProperties kafkaProperties) {
        return configurer.configureGlobalKafkaPropertiesSupplier(kafkaProperties);
    }

    @Bean(KafkaProducerPropertiesBuilderConfigurer.DEFAULT_BEAN_NAME)
    @ConditionalOnMissingBean(KafkaProducerPropertiesBuilderConfigurer.class)
    KafkaProducerPropertiesBuilderConfigurer configureProducerPropertiesBuilder(GlobalKafkaPropertiesSupplier globalKafkaPropertiesSupplier) {
        return (globalKafkaProperties, deckardKafkaPropertiesSupplier) -> kafkaProducer -> {
            Map<String, Object> producerProperties =  globalKafkaPropertiesSupplier.get().buildProducerProperties();
            final Map<String, DeckardKafkaProperties> deckardKafkaPropertiesMap = deckardKafkaPropertiesSupplier.get();
            if (deckardKafkaPropertiesMap.containsKey(kafkaProducer.id())) {
                final Map<String, Object> productKeyBasedProperties = deckardKafkaPropertiesMap.get(kafkaProducer.id()).buildProducerProperties();
                producerProperties.putAll(productKeyBasedProperties);
            }
            return producerProperties;
        };
    }

    @Bean(KafkaProducerPropertiesBuilder.DEFAULT_BEAN_NAME)
    KafkaProducerPropertiesBuilder kafkaProducerPropertiesBuilder(KafkaProducerPropertiesBuilderConfigurer configurer, KafkaProperties kafkaProperties, DeckardKafkaPropertiesSupplier deckardKafkaPropertiesSupplier) {
        return configurer.configureProducerPropertiesBuilder(kafkaProperties, deckardKafkaPropertiesSupplier);
    }
}
