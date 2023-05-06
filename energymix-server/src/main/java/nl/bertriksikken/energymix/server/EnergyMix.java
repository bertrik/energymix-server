package nl.bertriksikken.energymix.server;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson data class for publishing on the REST endpoint.
 */
public final class EnergyMix {

    @JsonProperty("time")
    public final long time;

    @JsonProperty("datetime")
    public final String dateTime;

    @JsonProperty("total")
    public int total = 0;

    @JsonProperty("mix")
    private List<EnergyComponent> mix = new ArrayList<>();

    EnergyMix(long time, String dateTime) {
        this.time = time;
        this.dateTime = dateTime;
    }

    public void addComponent(String id, double power, String color) {
        if (Double.isFinite(power)) {
            int megawatt = (int) Math.round(power);
            mix.add(new EnergyComponent(id, megawatt, color));
            total += megawatt;
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "{time=%s,total=%d,mix=%s}", Instant.ofEpochSecond(time), total, mix);
    }

    private static final class EnergyComponent {
        @JsonProperty("id")
        private final String id;

        @JsonProperty("power")
        private final int power;

        @JsonProperty("color")
        private final String color;

        private EnergyComponent(String id, int megawatt, String color) {
            this.id = id;
            this.power = megawatt;
            this.color = color;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%s=%d", id, power);
        }
    }

}
