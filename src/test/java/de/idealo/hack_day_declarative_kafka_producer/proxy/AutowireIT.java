package de.idealo.hack_day_declarative_kafka_producer.proxy;

import de.idealo.hack_day_declarative_kafka_producer.producer.GenericProducer;
import de.idealo.hack_day_declarative_kafka_producer.stereotype.KafkaProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AutowireIT {

    @Autowired
    private ApplicationContext context;

    @Test
    public void shouldInvokeHandler() {
        assertThat(context.getBean(TestConfig.MyTestProducer.class)).isNotNull();
    }


    @TestConfiguration
    private static class TestConfig {

        @KafkaProducer(topic = "someTopic")
        interface MyTestProducer extends GenericProducer<String, Object> {}
    }

}
