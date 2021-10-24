package de.idealo.kafka.deckard.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.key-serializer: org.apache.kafka.common.serialization.LongSerializer",
        "spring.kafka.producer.value-serializer: org.apache.kafka.common.serialization.IntegerSerializer"
})
@DirtiesContext
class CaseUtilTest {

    @Test
    void shouldSplitCamelCase() {
        Assertions.assertThat(CaseUtil.splitCamelCase("test")).containsOnly("test");
        Assertions.assertThat(CaseUtil.splitCamelCase("testAnotherCase")).containsSequence("test", "another", "case");
        Assertions.assertThat(CaseUtil.splitCamelCase("testABC")).containsOnly("test", "abc");
        Assertions.assertThat(CaseUtil.splitCamelCase("TestTest")).containsOnly("test", "test");
    }
}
