package de.idealo.hack_day_declarative_kafka_producer.stereotype;

import de.idealo.hack_day_declarative_kafka_producer.configuration.KafkaProducerConfiguration;
import de.idealo.hack_day_declarative_kafka_producer.proxy.BeanDefinitionRegistrar;
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
