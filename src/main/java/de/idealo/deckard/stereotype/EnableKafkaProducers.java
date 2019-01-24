package de.idealo.deckard.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import de.idealo.deckard.configuration.KafkaProducerAutoConfiguration;
import de.idealo.deckard.proxy.BeanDefinitionRegistrar;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ KafkaProducerAutoConfiguration.class, BeanDefinitionRegistrar.class /*,ProducerBeanRegistrar.class*/})
public @interface EnableKafkaProducers {
}
