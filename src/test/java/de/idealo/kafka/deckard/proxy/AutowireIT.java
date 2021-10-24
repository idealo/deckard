package de.idealo.kafka.deckard.proxy;

import de.idealo.kafka.deckard.TestApplication;
import de.idealo.kafka.deckard.producer.GenericProducer;
import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutowireIT {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldInvokeHandler() {
        assertThat(context.getBean(MyTestProducer.class)).isNotNull();
    }

    @KafkaProducer(topic = "someTopic")
    interface MyTestProducer extends GenericProducer<String, Object> {
    }

}
