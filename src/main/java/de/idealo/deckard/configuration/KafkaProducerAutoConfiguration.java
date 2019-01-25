package de.idealo.deckard.configuration;

import de.idealo.deckard.proxy.ProxyBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class KafkaProducerAutoConfiguration {

    @Bean("proxyBeanFactory")
    ProxyBeanFactory proxyBeanFactory(KafkaProperties kafkaProperties) {
        return new ProxyBeanFactory(kafkaProperties);
    }
}
