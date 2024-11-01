package nl.bertriksikken.entsoe;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.google.common.base.Strings;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EntsoeResponse(@JsonProperty("mRID") String mrid,
                             @JsonProperty("type") String type,
                             @JsonProperty("createdDateTime") String createdDateTime,
                             @JsonProperty("TimeSeries") @JacksonXmlElementWrapper(useWrapping = false) List<TimeSeries> timeSeries) {
    public EntsoeResponse() {
        this("", "", "", new ArrayList<>());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TimeSeries(
            @JsonProperty("mRID") String mrid,
            @JsonProperty("inBiddingZone_Domain.mRID") String inBiddingZoneDomainMrid,
            @JsonProperty("MktPSRType") MktPSRType psrType,
            @JsonProperty("Period") @JacksonXmlElementWrapper(useWrapping = false) List<Period> period) {
        public boolean isGeneration() {
            return !Strings.isNullOrEmpty(inBiddingZoneDomainMrid);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MktPSRType(@JsonProperty("psrType") EPsrType psrType) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Period(@JsonProperty("timeInterval") TimeInterval timeInterval,
                         @JsonProperty("resolution") String resolution,
                         @JsonProperty("Point") @JacksonXmlElementWrapper(useWrapping = false) List<Point> points) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
    public record TimeInterval(@JsonProperty("start") String start, @JsonProperty("end") String end) {
        public Instant getStart() {
            return DateTimeFormatter.ISO_DATE_TIME.parse(start, Instant::from);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Point(@JsonProperty("position") int position,
                        @JsonProperty("quantity") int quantity,
                        @JsonProperty("price.amount") double priceAmount) {
    }
}