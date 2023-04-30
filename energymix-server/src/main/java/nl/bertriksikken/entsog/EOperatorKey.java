package nl.bertriksikken.entsog;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EOperatorKey {

    UNKNOWN("", "Unknown"), //
    GTS("NL-TSO-0001", "Gasunie Transport Services B.V.");

    private final String key;
    final String label;

    EOperatorKey(String key, String label) {
        this.key = key;
        this.label = label;
    }

    @JsonValue
    String getKey() {
        return key;
    }

    @JsonCreator
    static EOperatorKey fromKey(String key) {
        return Stream.of(values()).filter(v -> v.key.equals(key)).findFirst().orElse(UNKNOWN);
    }

}
