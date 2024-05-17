package nl.bertriksikken.ned;

public enum EPoint {
    NEDERLAND(0),
    GRONINGEN(1),
    FRIESLAND(2),
    DRENTHE(3),
    OVERIJSSEL(4),
    FLEVOLAND(5),
    GELDERLAND(6),
    UTRECHT(7),
    NOORD_HOLLAND(8),
    ZUID_HOLLAND(9),
    ZEELAND(10),
    NOORD_BRABANT(11),
    LIMBURG(12);

    private final int value;

    EPoint(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
