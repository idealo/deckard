package de.idealo.deckard.configuration;

import de.idealo.deckard.proxy.ProducerProxyBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@AutoConfigureAfter(KafkaAutoConfiguration.class)
@Configuration
@Slf4j
public class KafkaProducerAutoConfiguration {

    @Autowired(required = false)
    private KafkaProperties kafkaProperties;
    @Autowired
    private ApplicationContext applicationContext;

    @Bean(ProducerProxyBeanFactory.DEFAULT_FACTORY_BEAN_NAME)
    ProducerProxyBeanFactory proxyBeanFactory(ConfigurableBeanFactory configurableBeanFactory) {
        KafkaProperties properties = Optional.ofNullable(this.kafkaProperties).orElseGet(() -> {
            log.warn("KafkaProperties were not provided by Spring, creating default. This should only happen in test scenarios.");
            return new KafkaProperties();
        });

        return new ProducerProxyBeanFactory(properties, configurableBeanFactory, applicationContext);
    }
}
