package nl.bertriksikken.entsoe;

/**
 * Document type definition.
 *
 * @see <a href="https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html#_documenttype">document type</a>
 */
public enum EDocumentType {

    PRICE_DOCUMENT("A44"),    //
    INSTALLED_CAPACITY_PER_TYPE("A68"), //
    WIND_SOLAR_FORECAST("A69"), //
    ACTUAL_GENERATION_PER_TYPE("A75");

    private final String code;

    EDocumentType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
