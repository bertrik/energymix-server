package nl.bertriksikken.entsoe;

public enum EArea {

    NETHERLANDS("10YNL----------L"); //

    private final String code;

    EArea(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
