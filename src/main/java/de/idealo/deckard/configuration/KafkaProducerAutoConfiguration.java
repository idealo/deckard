package de.idealo.deckard.configuration;

import de.idealo.deckard.proxy.ProxyBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@AutoConfigureAfter(KafkaAutoConfiguration.class)
@Configuration
@Slf4j
public class KafkaProducerAutoConfiguration {

    @Autowired(required = false)
    private KafkaProperties kafkaProperties;

    @Bean("proxyBeanFactory")
    ProxyBeanFactory proxyBeanFactory() {
        KafkaProperties properties = Optional.ofNullable(this.kafkaProperties).orElseGet(() -> {
            log.warn("KafkaProperties were not provided by Spring, creating default. This should only happen in test scenarios.");
            return new KafkaProperties();
        });

        return new ProxyBeanFactory(properties);
    }
}
