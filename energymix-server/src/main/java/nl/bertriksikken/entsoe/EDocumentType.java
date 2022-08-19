package nl.bertriksikken.entsoe;

/**
 * @see https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html#_documenttype
 */
public enum EDocumentType {

    PRICE_DOCUMENT("A44"),    //
    WIND_SOLAR_FORECAST("A69"), //
    ACTUAL_GENERATION_PER_TYPE("A75");

    private final String code;

    EDocumentType(String code) {
        this.code = code;
    }

    String getCode() {
        return code;
    }

}
