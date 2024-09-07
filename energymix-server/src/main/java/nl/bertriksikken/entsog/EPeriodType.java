package nl.bertriksikken.entsog;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EPeriodType {
    UNKNOWN("unknown"), //
    DAY("day"), //
    HOUR("hour");

    private final String code;

    EPeriodType(String code) {
        this.code = code;
    }

    @JsonCreator
    public static EPeriodType create(String code) {
        return Stream.of(values()).filter(t -> t.code.equals(code)).findFirst().orElse(EPeriodType.UNKNOWN);
    }

    @JsonValue
    String getCode() {
        return code;
    }
}
