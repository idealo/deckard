package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.properties.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static java.util.Collections.emptyMap;

@Data
@Slf4j
@AutoConfigureAfter({KafkaAutoConfiguration.class})
@ConfigurationProperties(prefix = "deckard")
@Configuration
public class DeckardPropertiesAutoConfiguration {

    private Features features = new Features();
    private Map<String, DeckardKafkaProperties> properties = emptyMap();

    @Bean(GlobalKafkaProducerPropertiesBuilderConfigurer.DEFAULT_BEAN_NAME)
    @ConditionalOnMissingBean(GlobalKafkaProducerPropertiesBuilderConfigurer.class)
    GlobalKafkaProducerPropertiesBuilderConfigurer globalKafkaProducerPropertiesBuilderConfigurer() {
        return new DefaultGlobalKafkaProducerPropertiesBuilderConfigurer();
    }

    @Bean(GlobalKafkaProducerPropertiesBuilder.DEFAULT_BEAN_NAME)
    GlobalKafkaProducerPropertiesBuilder globalKafkaProducerPropertiesBuilder(GlobalKafkaProducerPropertiesBuilderConfigurer configurer, KafkaProperties kafkaProperties) {
        return configurer.configureGlobalKafkaProducerPropertiesBuilder(kafkaProperties);
    }

    @Bean(DeckardKafkaPropertiesSupplier.DEFAULT_BEAN_NAME)
    public DeckardKafkaPropertiesSupplier deckardKafkaPropertiesSupplier() {
        return () -> properties;
    }

    @Bean(ContextPropertyKafkaProducerPropertiesBuilderConfigurer.DEFAULT_BEAN_NAME)
    @ConditionalOnMissingBean(ContextPropertyKafkaProducerPropertiesBuilderConfigurer.class)
    ContextPropertyKafkaProducerPropertiesBuilderConfigurer contextPropertyKafkaProducerPropertiesBuilderConfigurer() {
        return new DefaultContextPropertyKafkaProducerPropertiesBuilderConfigurer();
    }

    @Bean(ContextPropertyKafkaProducerPropertiesBuilder.DEFAULT_BEAN_NAME)
    ContextPropertyKafkaProducerPropertiesBuilder contextPropertyKafkaProducerPropertiesBuilder(ContextPropertyKafkaProducerPropertiesBuilderConfigurer configurer, DeckardKafkaPropertiesSupplier deckardKafkaPropertiesSupplier) {
        return configurer.configureContextPropertyKafkaProducerPropertiesBuilder(deckardKafkaPropertiesSupplier);
    }

    @Bean(ClientIdBuilder.DEFAULT_BEAN_NAME)
    @ConditionalOnMissingBean(ClientIdBuilder.class)
    ClientIdBuilder clientIdBuilder() {
        if (features.getAutoGenerateClientId().isEnabled()) {
            return new DefaultClientIdBuilder();
        } else {
            return new PassThroughClientIdBuilder();
        }
    }

    @Bean(AnnotationKafkaProducerPropertiesBuilder.DEFAULT_BEAN_NAME)
    AnnotationKafkaProducerPropertiesBuilder annotationKafkaProducerPropertiesBuilder(ConfigurableBeanFactory configurableBeanFactory) {
        EmbeddedValueResolver embeddedValueResolver = new EmbeddedValueResolver(configurableBeanFactory);
        return new DefaultAnnotationKafkaProducerPropertiesBuilder(embeddedValueResolver);
    }

    @Bean(ProducerPropertiesResolver.DEFAULT_BEAN_NAME)
    ProducerPropertiesResolver producerPropertiesResolver(
            GlobalKafkaProducerPropertiesBuilder globalKafkaProducerPropertiesBuilder,
            ContextPropertyKafkaProducerPropertiesBuilder kafkaProducerPropertiesBuilder,
            AnnotationKafkaProducerPropertiesBuilder annotationKafkaProducerPropertiesBuilder,
            ClientIdBuilder clientIdBuilder) {
        return new ProducerPropertiesResolver(globalKafkaProducerPropertiesBuilder, kafkaProducerPropertiesBuilder, annotationKafkaProducerPropertiesBuilder, clientIdBuilder);
    }

    @Data
    public static final class Features {

        private Feature autoGenerateClientId = new Feature().setEnabled(true);

        @Data
        @Accessors(chain = true)
        public static final class Feature {
            private boolean enabled;
        }
    }
}
