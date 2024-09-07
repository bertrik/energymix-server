package nl.bertriksikken.entsoe;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

/**
 * Area definition.
 *
 * @see <a href="https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html#_areas">areas</a>
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
