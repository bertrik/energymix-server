package nl.bertriksikken.ned;

public enum EGranularity {
    TEN_MINUTES(3), FIFTEEN_MINUTES(4), HOUR(5), DAY(6), MONTH(7), YEAR(8);

    private final int value;

    EGranularity(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public enum ETimeZone {
        UTC(0), CET(1);

        private final int value;

        ETimeZone(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
