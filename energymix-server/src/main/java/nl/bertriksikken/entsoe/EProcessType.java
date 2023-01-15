package nl.bertriksikken.entsoe;

/**
 * @see https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html#_processtype
 */
public enum EProcessType {

    DAY_AHEAD("A01"), //
    REALISED("A16"), //
    YEAR_AHEAD("A33");

    private final String code;

    EProcessType(String code) {
        this.code = code;
    }

    String getCode() {
        return code;
    }

}
