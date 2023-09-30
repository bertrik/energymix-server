package nl.bertriksikken.entsog;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

@JsonAutoDetect(getterVisibility = Visibility.NONE)
public final class EntsogAggregatedData {

    @JsonProperty("meta")
    public JsonNode meta = NullNode.getInstance();

    @JsonProperty("aggregatedData")
    public List<AggregatedData> aggregatedData = new ArrayList<>();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class AggregatedData {
        @JsonProperty("id")
        public String id = "";

        @JsonProperty("indicator")
        public EIndicator indicator = EIndicator.UNKNOWN;

        @JsonProperty("periodType")
        public EPeriodType periodType = EPeriodType.UNKNOWN;

        @JsonProperty("periodFrom")
        public OffsetDateTime periodFrom = OffsetDateTime.MIN;
        @JsonProperty("periodTo")
        public OffsetDateTime periodTo = OffsetDateTime.MIN;

        @JsonProperty("operatorKey")
        public EOperatorKey operatorKey = EOperatorKey.UNKNOWN;

        @JsonProperty("tsoEicCode")
        public String tsoEic = "";

        @JsonProperty("directionKey")
        public EDirectionKey directionKey = EDirectionKey.UNKNOWN;

        @JsonProperty("adjacentSystemsKey")
        public EAdjacentSystemsKey adjacentSystemsKey = EAdjacentSystemsKey.UNKNOWN;

        @JsonProperty("year")
        private int year;

        @JsonProperty("month")
        private int month;

        @JsonProperty("day")
        private int day;

        public LocalDate getDate() {
            return LocalDate.of(year, month, day);
        }

        @JsonProperty("unit")
        public String unit = "";

        @JsonProperty("value")
        public double value = Double.NaN;

        @JsonProperty("lastUpdateDateTime")
        public OffsetDateTime lastUpdateDateTime = OffsetDateTime.MIN;

        @Override
        public String toString() {
            return id;
        }
    }
}
