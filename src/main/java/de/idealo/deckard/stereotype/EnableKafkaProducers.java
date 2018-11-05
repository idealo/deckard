package de.idealo.deckard.stereotype;

import de.idealo.deckard.configuration.KafkaProducerConfiguration;
import de.idealo.deckard.proxy.BeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({KafkaProducerConfiguration.class, BeanDefinitionRegistrar.class})
public @interface EnableKafkaProducers {
}
