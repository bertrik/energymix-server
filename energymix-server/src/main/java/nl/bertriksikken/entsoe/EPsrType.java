package nl.bertriksikken.entsoe;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

/**
 * PSR type definition.
 *
 * @see <a href="https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html#_psrtype">PSR type</a>
 */
public enum EPsrType {

    UNKNOWN("", "Unknown"), //

    MIXED("A03", "Mixed"), //
    GENERATION("A04", "Generation"), //
    LOAD("A05", "Load"), //

    BIOMASS("B01", "Biomass"), //
    FOSSIL_BROWN_COAL("B02", "Fossil brown coal/Lignite"), //
    FOSSIL_COAL_DERIVED_GAS("B03", "Fossil coal-derived gas"), //
    FOSSIL_GAS("B04", "Fossil Gas"), //
    FOSSIL_HARD_COAL("B05", "Fossil Hard coal"), //
    FOSSIL_OIL("B06", "Fossil oil"), //
    FOSSIL_OIL_SHALE("B07", "Fossil oil shale"), //
    FOSSIL_PEAT("B08", "Fossil peat"), //
    GEOTHERMAL("B09", "Geothermal"), //
    HYDRO_PUMPED_STORAGE("B10", "Hydro Pumped Storage"), //
    HYDRO_RIVER_POUNDAGE("B11", "Hydro run-of-river and poundage"), //
    HYDRO_RESERVOIR("B12", "Hydro Water Reservoir"), //
    MARINE("B13", "Marine"), //
    NUCLEAR("B14", "Nuclear"), //
    OTHER_RENEWABLE("B15", "Other renewable"), //
    SOLAR("B16", "Solar"), //
    WASTE("B17", "Waste"), //
    WIND_OFFSHORE("B18", "Wind Offshore"), //
    WIND_ONSHORE("B19", "Wind Onshore"), //
    OTHER("B20", "Other"), //

    AC_LINK("B21", "AC Link"), //
    DC_LINK("B22", "DC Link"), //
    SUBSTATION("B23", "Substation"), //
    TRANSFORMER("B24", "Transformer"); //

    private final String code;
    private final String description;

    EPsrType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static EPsrType create(String code) {
        return Stream.of(values()).filter(t -> t.code.equals(code)).findFirst().orElse(EPsrType.UNKNOWN);
    }

}
