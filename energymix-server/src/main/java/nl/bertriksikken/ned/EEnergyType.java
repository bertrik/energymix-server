package nl.bertriksikken.ned;

public enum EEnergyType {
    ALL(0),
    WIND(1),
    SOLAR(2),
    BIOGAS(3),
    HEATPUMP(4),
    COFIRING(8),
    GEOTHERMAL(9),
    OTHER(10),
    WASTE(11),
    BIO_OIL(12),
    BIO_MASS(13),
    WOOD(14),
    WIND_OFFSHORE(17),
    FOSSIL_GAS_POWER(18),
    FOSSIL_HARD_COAL(19),
    NUCLEAR(20),
    WASTE_POWER(21),
    WIND_OFFSHORE_B(22),
    NATURAL_GAS(23),
    BIO_METHANE(24),
    BIOMASS_POWER(25),
    OTHER_POWER(26),
    ELECTRICITY_MIX(27),
    GAS_MIX(28),
    GAS_DISTRIBUTION(31),
    WKK_TOTAL(35),
    SOLAR_THERMAL(50),
    WIND_OFFSHORE_C(51),
    INDUSTRIAL_CONSUMERS_GAS_COMBINATION(53),
    INDUSTRIAL_CONSUMERS_POWER_GAS_COMBINATION(54),
    LOCAL_DISTRIBUTION_COMPANIES_COMBINATION(55),
    ALL_CONSUMING_GAS(56);

    private final int value;

    EEnergyType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
