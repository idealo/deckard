package de.idealo.kafka.deckard.configuration;

import de.idealo.kafka.deckard.properties.ProducerPropertiesResolver;
import de.idealo.kafka.deckard.proxy.ProducerProxyBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfigureAfter({KafkaAutoConfiguration.class, DeckardPropertiesAutoConfiguration.class})
@Configuration
@Slf4j
public class KafkaProducerAutoConfiguration {

    @Bean(ProducerProxyBeanFactory.DEFAULT_FACTORY_BEAN_NAME)
    ProducerProxyBeanFactory proxyBeanFactory(ConfigurableBeanFactory configurableBeanFactory, ProducerPropertiesResolver producerPropertiesResolver, ApplicationContext applicationContext) {
        return new ProducerProxyBeanFactory(producerPropertiesResolver, configurableBeanFactory, applicationContext);
    }
}
