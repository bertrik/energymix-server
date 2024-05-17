package nl.bertriksikken.ned;

public enum EActivity {
    PROVIDING(1),
    CONSUMING(2);

    private final int value;

    EActivity(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
