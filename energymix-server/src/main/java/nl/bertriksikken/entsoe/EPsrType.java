package nl.bertriksikken.entsoe;

public enum EPsrType {

    GENERATION("A04"), //
    SOLAR("B16"), //
    WIND_ONSHORE("B18"), //
    WIND_OFFSHORE("B19"); //

    private final String code;

    EPsrType(String code) {
        this.code = code;
    }

    String getCode() {
        return code;
    }

}
