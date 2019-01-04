package de.idealo.deckard.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KafkaProducer {

    String topic();
    Class serializer() default DefaultSerializer.class;

    class DefaultSerializer {}
}
