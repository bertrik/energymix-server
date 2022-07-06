package nl.bertriksikken.energymix.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson data class for publishing on the REST endpoint.
 */
public final class EnergyMix {
    
    @JsonProperty("time")
    public long time;
    
    @JsonProperty("total")
    public long total;
    
    @JsonProperty("mix")
    private List<EnergyComponent> mix = new ArrayList<>();
    
    public EnergyMix(long time, long total) {
        this.time = time;
        this.total = total;
    }
    
    public void addComponent(String id, double power, String color) {
        mix.add(new EnergyComponent(id, Math.round(power), color));
    }
    
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "{time=%d,total=%d,mix=%s}", time, total, mix);
    }
    
    private static final class EnergyComponent {
        @JsonProperty("id")
        private final String id;
        
        @JsonProperty("power")
        private final long power;
        
        @JsonProperty("color")
        private final String color;
        
        private EnergyComponent(String id, long megawatt, String color) {
            this.id = id;
            this.power = megawatt;
            this.color = color;
        }
        
        @Override
        public String toString() {
            return String.format(Locale.ROOT, "{id=%s,power=%d,color=%s}", id, power, color);
        }
    }

}
