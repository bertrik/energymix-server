package nl.bertriksikken.entsog;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EDirectionKey {

    UNKNOWN("unknown"), //
    EXIT("exit"), //
    ENTRY("entry");

    private final String key;

    EDirectionKey(String key) {
        this.key = key;
    }

    @JsonValue
    String getKey() {
        return key;
    }

    @JsonCreator
    static EDirectionKey fromKey(String key) {
        return Stream.of(values()).filter(v -> v.key.equals(key)).findFirst().orElse(UNKNOWN);
    }

}
