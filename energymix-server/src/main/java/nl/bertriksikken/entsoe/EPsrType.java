package nl.bertriksikken.entsoe;

public enum EPsrType {

    MIXED("A03"), //
    GENERATION("A04"), //
    LOAD("A05"), //

    BIOMASS("B01"), //
    FOSSIL_BROWN_COAL("B02"), //
    FOSSIL_COAL_DERIVED_GAS("B03"), //
    FOSSIL_GAS("B04"), //
    FOSSIL_HARD_COAL("B05"), //
    FOSSIL_OIL("B06"), //
    FOSSIL_OIL_SHALE("B07"), //
    FOSSIL_PEAT("B08"), //
    GEOTHERMAL("B09"), //
    HYDRO_PUMPED_STORAGE("B10"), //
    HYDRO_RIVER_POUNDAGE("B11"), //
    HYDRO_RESERVOIR("B12"), //
    MARINE("B13"), //
    NUCLEAR("B14"), //
    RENEWABLE_OTHER("B15"), //
    SOLAR("B16"), //
    WASTE("B17"), //
    WIND_ONSHORE("B18"), //
    WIND_OFFSHORE("B19"), //
    OTHER("B20"), //

    AC_LINK("B21"), //
    DC_LINK("B22"), //
    SUBSTATION("B23"), //
    TRANSFORMER("B24"); //

    private final String code;

    EPsrType(String code) {
        this.code = code;
    }

    String getCode() {
        return code;
    }

}
