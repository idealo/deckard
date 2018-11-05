package de.idealo.deckard.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public final class CaseUtil {

    private static final String CAMEL_CASE_REGEX = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";

    private CaseUtil() {
    }

    public static List<String> splitCamelCase(String camelCase) {
        List<String> result = new LinkedList<>();
        for (String w : camelCase.split(CAMEL_CASE_REGEX)) {
            result.add(w.toLowerCase(Locale.getDefault()));
        }
        return result;
    }
}
