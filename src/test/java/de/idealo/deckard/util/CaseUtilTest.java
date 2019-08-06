package de.idealo.deckard.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static de.idealo.deckard.util.CaseUtil.splitCamelCase;
import static org.assertj.core.api.Assertions.assertThat;
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.key-serializer: org.apache.kafka.common.serialization.LongSerializer",
        "spring.kafka.producer.value-serializer: org.apache.kafka.common.serialization.IntegerSerializer"
})
@DirtiesContext
public class CaseUtilTest {

    @Test
    public void shouldSplitCamelCase() {
        assertThat(splitCamelCase("test")).containsOnly("test");
        assertThat(splitCamelCase("testAnotherCase")).containsSequence("test", "another", "case");
        assertThat(splitCamelCase("testABC")).containsOnly("test", "abc");
        assertThat(splitCamelCase("TestTest")).containsOnly("test", "test");
    }
}