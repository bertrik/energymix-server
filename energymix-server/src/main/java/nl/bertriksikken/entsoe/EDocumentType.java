package nl.bertriksikken.entsoe;

public enum EDocumentType {

    WIND_SOLAR_FORECAST("A69"),
    ACTUAL_GENERATION_PER_TYPE("A75"),
    ;
    
    private final String code;
    
    EDocumentType(String code) {
        this.code = code;
    }

    String getCode() {
        return code;
    }
    
}
