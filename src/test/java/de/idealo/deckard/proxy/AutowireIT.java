package de.idealo.deckard.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import de.idealo.deckard.producer.GenericProducer;
import de.idealo.deckard.stereotype.KafkaProducer;
import de.idealo.junit.rules.TestLoggerRuleFactory;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AutowireIT {

    @Rule
    public TestRule testLogger = TestLoggerRuleFactory.silent();

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
