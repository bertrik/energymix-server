package nl.bertriksikken.entsog;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EAdjacentSystemsKey {
    UNKNOWN("Unknown", "Unknown"), //

    LNG_TERMINALS("LNG Terminals", "LNG Terminals"), //
    PRODUCTION("Production", "Production"), //
    STORAGE("Storage", "Storage"), //
    TRANSMISSION("Transmission", "Transmission"), //
    BELUX("TransmissionBE-LUX------", "BeLux"), //
    DE_THE_BZ("TransmissionDE-THE-----", "DE THE BZ"), //
    DISTRIBUTION("Distribution", "Distribution"), //
    FINAL_CONSUMERS("Final Consumers", "Final Consumers"), //

    ;

    private final String key;
    private final String label;

    EAdjacentSystemsKey(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static EAdjacentSystemsKey fromKey(String key) {
        return Stream.of(values()).filter(v -> v.key.equals(key)).findFirst().orElse(UNKNOWN);
    }

}
