package nl.bertriksikken.entsoe;

public enum EProcessType {

    DAY_AHEAD("A01"), //
    REALISED("A16"); //

    private final String code;

    EProcessType(String code) {
        this.code = code;
    }

    String getCode() {
        return code;
    }

}
