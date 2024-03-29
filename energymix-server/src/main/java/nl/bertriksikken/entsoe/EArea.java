package nl.bertriksikken.entsoe;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @see https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html#_areas
 */
public enum EArea {

    UNKNOWN(""), //
    NETHERLANDS("10YNL----------L"); // currently the only defined one

    private final String code;

    EArea(String code) {
        this.code = code;
    }

    @JsonCreator
    public static EArea create(String code) {
        return Stream.of(values()).filter(t -> t.code.equals(code)).findFirst().orElse(EArea.UNKNOWN);
    }

    @JsonValue
    public String getCode() {
        return code;
    }

}
