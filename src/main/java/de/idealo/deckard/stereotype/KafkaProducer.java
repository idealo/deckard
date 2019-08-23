package de.idealo.deckard.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KafkaProducer {

    String topic();

    String[] bootstrapServers() default {};

    Class keySerializer() default DefaultSerializer.class;

    Class valueSerializer() default DefaultSerializer.class;

    class DefaultSerializer {
    }
}
