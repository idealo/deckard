package de.idealo.kafka.deckard.proxy;

import de.idealo.kafka.deckard.TestApplication;
import de.idealo.kafka.deckard.producer.GenericProducer;
import de.idealo.kafka.deckard.stereotype.KafkaProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@RunWith(SpringRunner.class)
public class AutowireIT {

    @Autowired
    private ApplicationContext context;

    @Test
    public void shouldInvokeHandler() {
        assertThat(context.getBean(MyTestProducer.class)).isNotNull();
    }

    @KafkaProducer(topic = "someTopic")
    interface MyTestProducer extends GenericProducer<String, Object> {}

}
