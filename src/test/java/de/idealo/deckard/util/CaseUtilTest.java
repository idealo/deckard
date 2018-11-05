package de.idealo.deckard.util;

import org.junit.Test;

import static de.idealo.deckard.util.CaseUtil.splitCamelCase;
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