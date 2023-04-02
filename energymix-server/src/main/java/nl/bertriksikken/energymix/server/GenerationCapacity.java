package nl.bertriksikken.energymix.server;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON for reporting installed generation capacity per production type
 */
public final class GenerationCapacity {

    @JsonProperty("total")
    public int total = 0;

    @JsonProperty("capacity")
    private final List<Capacity> capacities = new ArrayList<>();

    public void add(String id, String description, int power) {
        capacities.add(new Capacity(id, description, power));
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
    }
}
