package de.idealo.kafka.deckard.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated <code>EnableKafkaProducers</code> is no longer required as the wiring is done with auto configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface EnableKafkaProducers {
}
