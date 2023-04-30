package nl.bertriksikken.entsog;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EIndicator {

    UNKNOWN("Unknown"), //
    NOMINATION_RENOMINATION("Nomination/Renomination"), //
    ALLOCATION("Allocation"), //
    PHYSICAL_FLOW("Physical Flow");

    private final String code;

    EIndicator(String code) {
        this.code = code;
    }

    @JsonValue
    String getCode() {
        return code;
    }

    @JsonCreator
    public static EIndicator fromCode(String code) {
        return Stream.of(values()).filter(v -> v.code.equals(code)).findFirst().orElse(UNKNOWN);
    }

}
