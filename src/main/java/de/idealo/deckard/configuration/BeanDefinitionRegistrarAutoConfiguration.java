package de.idealo.deckard.configuration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import de.idealo.deckard.proxy.BeanDefinitionRegistrar;

@AutoConfigureAfter(KafkaProducerAutoConfiguration.class)
@Configuration
@Import(BeanDefinitionRegistrar.class)
public class BeanDefinitionRegistrarAutoConfiguration {
}
