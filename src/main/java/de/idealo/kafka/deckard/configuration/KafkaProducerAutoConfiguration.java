package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.proxy.ProducerPropertiesResolver;
import de.idealo.kafka.deckard.proxy.ProducerProxyBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfigureAfter(KafkaAutoConfiguration.class)
@Configuration
@Slf4j
public class KafkaProducerAutoConfiguration {

    @Autowired(required = false)
    private KafkaProperties kafkaProperties;
    @Autowired
    private ApplicationContext applicationContext;

    @Bean(ProducerPropertiesResolver.DEFAULT_BEAN_NAME)
    ProducerPropertiesResolver producerPropertiesResolver(ConfigurableBeanFactory configurableBeanFactory) {
        return new ProducerPropertiesResolver(kafkaProperties, configurableBeanFactory);
    }

    @Bean(ProducerProxyBeanFactory.DEFAULT_FACTORY_BEAN_NAME)
    ProducerProxyBeanFactory proxyBeanFactory(ConfigurableBeanFactory configurableBeanFactory, ProducerPropertiesResolver kafkaPropertiesResolver) {
        return new ProducerProxyBeanFactory(kafkaPropertiesResolver, configurableBeanFactory, applicationContext);
    }
}
