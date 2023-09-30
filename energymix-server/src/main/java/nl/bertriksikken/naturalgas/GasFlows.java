package nl.bertriksikken.naturalgas;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import nl.bertriksikken.entsog.EAdjacentSystemsKey;
import nl.bertriksikken.entsog.EDirectionKey;

public final class GasFlows {

    @JsonProperty("date")
    final String date;

    @JsonProperty("flows")
    final List<GasFlow> flows = new ArrayList<>();

    @JsonProperty("lastUpdated")
    final String lastUpdated;

    public GasFlows(String date, String lastUpdated) {
        this.date = date;
        this.lastUpdated = lastUpdated;
    }

    public void addFlow(EAdjacentSystemsKey system, EDirectionKey direction, int value) {
        GasFlow flow = getOrCreate(system);
        if (direction.equals(EDirectionKey.ENTRY)) {
            flow.entry = value;
        } else {
            flow.exit = value;
        }
    }

    private GasFlow getOrCreate(EAdjacentSystemsKey system) {
        GasFlow flow = flows.stream().filter(f -> f.system.equals(system)).findFirst().orElse(null);
        if (flow == null) {
            flow = new GasFlow(system);
            flows.add(flow);
        }
        return flow;
    }

    @JsonInclude(Include.NON_NULL)
    static final class GasFlow {
        @JsonProperty("system")
        final EAdjacentSystemsKey system;
        @JsonProperty("entry")
        Integer entry = null;
        @JsonProperty("exit")
        Integer exit = null;

        private GasFlow(EAdjacentSystemsKey system) {
            this.system = system;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "{system=%s,entry=%d,exit=%d}", system, entry, exit);
        }
    }
}
