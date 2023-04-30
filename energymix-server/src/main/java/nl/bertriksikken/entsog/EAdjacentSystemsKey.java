package nl.bertriksikken.entsog;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EAdjacentSystemsKey {
    UNKNOWN("Unknown"), //

    LNG_TERMINALS("LNG Terminals"), //
    PRODUCTION("Production"), //
    STORAGE("Storage"), //
    DISTRIBUTION("Distribution"), //
    FINAL_CONSUMERS("Final Consumers"), //
    TRANSMISSION("Transmission");

    private final String key;

    EAdjacentSystemsKey(String key) {
        this.key = key;
    }

    @JsonValue
    public String getKey() {
        return key;
    }

    @JsonCreator
    public static EAdjacentSystemsKey fromKey(String key) {
        return Stream.of(values()).filter(v -> v.key.equals(key)).findFirst().orElse(UNKNOWN);
    }

}
