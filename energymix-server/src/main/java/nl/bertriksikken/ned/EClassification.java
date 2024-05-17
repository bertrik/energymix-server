package nl.bertriksikken.ned;

public enum EClassification {
    FORECAST(1),
    CURRENT(2),
    BACKCAST(3);

    private final int value;

    EClassification(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
