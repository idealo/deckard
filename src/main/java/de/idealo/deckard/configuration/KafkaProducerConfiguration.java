package de.idealo.deckard.configuration;

import de.idealo.deckard.proxy.ProxyBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
public class KafkaProducerConfiguration {

    @Bean
    ProxyBeanFactory proxyBeanFactory(KafkaProperties properties) {
        return new ProxyBeanFactory(properties);
    }

}
