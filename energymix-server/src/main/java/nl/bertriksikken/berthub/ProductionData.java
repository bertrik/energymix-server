package nl.bertriksikken.berthub;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * One line from the production data CSV.
 */
public final class ProductionData {

    private static final DateTimeFormatter DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(DAY_OF_MONTH, 2).appendLiteral('.').appendValue(MONTH_OF_YEAR, 2).appendLiteral('.')
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral(' ').appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2).toFormatter().withZone(ZoneOffset.UTC);

    private final boolean valid;
    public final Instant time;
    public final Double biomass;
    public final Double fossil;
    public final Double nuclear;
    public final Double other;
    public final Double solar;
    public final Double waste;
    public final Double wind;

    private ProductionData(boolean valid, Instant time, Double biomass, Double fossil, Double nuclear, Double other,
            Double solar, Double waste, Double wind) {
        this.valid = valid;
        this.time = time;
        this.biomass = biomass;
        this.fossil = fossil;
        this.nuclear = nuclear;
        this.other = other;
        this.solar = solar;
        this.waste = waste;
        this.wind = wind;
    }

    public ProductionData withSolar(Double newSolar) {
        return new ProductionData(valid, time, biomass, fossil, nuclear, other, newSolar, waste, wind);
    }

    // parses one line
    public static ProductionData parse(Map<String, String> data) {
        Instant time = parseMtu(data);

        // any data at all in it?
        if (!hasData(data)) {
            return new ProductionData(false, time, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                    Double.NaN, Double.NaN);
        }

        Double biomass = sumComponents(data, "Biomass");
        Double fossil = sumComponents(data, "Fossil");
        Double nuclear = sumComponents(data, "Nuclear");
        Double other = sumComponents(data, "Other");
        Double solar = sumComponents(data, "Solar");
        Double waste = sumComponents(data, "Waste");
        Double wind = sumComponents(data, "Wind");
        return new ProductionData(true, time, biomass, fossil, nuclear, other, solar, waste, wind);
    }

    public static Instant parseMtu(Map<String, String> data) {
        String mtu = data.getOrDefault("MTU", "");
        String withoutUtc = mtu.replaceAll("\\(UTC\\)", "");
        String[] times = withoutUtc.split("-");
        if (times.length == 2) {
            // parse second part
            String endTime = times[1].trim();
            return Instant.from(DATETIME_FORMATTER.parse(endTime));
        }
        return null;
    }

    // sums all contributions with a common prefix
    private static Double sumComponents(Map<String, String> data, String prefix) {
        double sum = 0.0;
        for (Entry<String, String> entry : data.entrySet()) {
            String column = entry.getKey();
            if (column.startsWith(prefix)) {
                try {
                    String value = entry.getValue();
                    sum += Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return sum;
    }

    // if any of the elements has "-", the line is considered invalid
    private static boolean hasData(Map<String, String> data) {
        for (String cell : data.values()) {
            if (cell.equals("-") || cell.equalsIgnoreCase("N/A")) {
                return false;
            }
        }
        return true;
    }

    public boolean isValid() {
        return valid;
    }

    public double getTotal() {
        return biomass + fossil + nuclear + other + solar + waste + wind;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "{time=%d,biomass=%.0f,fossil=%.0f,nuclear=%.0f,other=%.0f,solar=%.0f,waste=%.0f,wind=%.0f}",
                time.getEpochSecond(), biomass, fossil, nuclear, other, solar, waste, wind);
    }

}
