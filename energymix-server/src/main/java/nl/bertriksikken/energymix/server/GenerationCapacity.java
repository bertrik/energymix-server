package nl.bertriksikken.energymix.server;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * JSON for reporting installed generation capacity per production type.
 */
public final class GenerationCapacity {

    @JsonProperty("total")
    public int total = 0;

    @JsonProperty("capacity")
    private final List<Capacity> capacities = new ArrayList<>();

    public void add(String id, String description, int power) {
        capacities.add(new Capacity(id, description, power));
        capacities.sort(Comparator.comparing(c -> c.id));
        total += power;
    }

    private static final class Capacity {
        @JsonProperty("id")
        private final String id;
        @JsonProperty("description")
        private final String description;
        @JsonProperty("power")
        private final int power;

        private Capacity(String id, String description, int power) {
            this.id = id;
            this.description = description;
            this.power = power;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "{id=%s,description=%s,power=%d}", id, description, power);
        }
    }

}
