package nl.bertriksikken.ned;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class UtilizationJson {
    @JsonProperty("id")
    long id;
    @JsonProperty("point")
    String point = "";
    @JsonProperty("type")
    String type = "";
    @JsonProperty("granularity")
    String granularity = "";
    @JsonProperty("granularitytimezone")
    String granularitytimezone = "";
    @JsonProperty("activity")
    String activity = "";
    @JsonProperty("classification")
    String classification = "";

    @JsonProperty("capacity")
    long capacity;
    @JsonProperty("volume")
    public long volume;
    @JsonProperty("percentage")
    double percentage = Double.NaN;
    @JsonProperty("emission")
    double emission = Double.NaN;
    @JsonProperty("emissionfactor")
    double emissionFactor = Double.NaN;
    @JsonProperty("validfrom")
    public String validFrom = "";
    @JsonProperty("validto")
    String validTo = "";
    @JsonProperty("lastupdate")
    public String lastUpdate = "";

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "{type=%s,volume=%d,valid=%s-%s}", type, volume, validFrom, validTo);
    }

}
