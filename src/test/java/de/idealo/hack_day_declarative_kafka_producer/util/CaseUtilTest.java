package de.idealo.hack_day_declarative_kafka_producer.util;

import org.junit.Test;

import static de.idealo.hack_day_declarative_kafka_producer.util.CaseUtil.splitCamelCase;
import static org.assertj.core.api.Assertions.assertThat;

public class CaseUtilTest {

    @Test
    public void shouldSplitCamelCase() {
        assertThat(splitCamelCase("test")).containsOnly("test");
        assertThat(splitCamelCase("testAnotherCase")).containsSequence("test", "another", "case");
        assertThat(splitCamelCase("testABC")).containsOnly("test", "abc");
        assertThat(splitCamelCase("TestTest")).containsOnly("test", "test");
    }
}