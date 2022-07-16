package nl.bertriksikken.entsoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class EntsoeResponse {

    @JsonProperty("mRID")
    public String mrid = "";

    @JsonProperty("type")
    public String type = "";

    @JsonProperty(value = "TimeSeries")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<TimeSeries> timeSeries = new ArrayList<>();

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "{mRID=%s,type=%s,TimeSeries=%s}", mrid, type, timeSeries);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class TimeSeries {
        @JsonProperty("mRID")
        public String mrid = "";

        @JsonProperty("MktPSRType")
        public MktPSRType psrType = new MktPSRType();

        @JsonProperty("Period")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Period> period = new ArrayList<>();

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "{mRID=%s,MktPSRType=%s,Period=%s}", mrid, psrType, period);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class MktPSRType {
        @JsonProperty("psrType")
        public String psrType = "";

        @Override
        public String toString() {
            return psrType;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Period {
        @JsonProperty("timeInterval")
        public TimeInterval timeInterval;

        @JsonProperty("resolution")
        public String resolution = "";

        @JsonProperty("Point")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Point> points = new ArrayList<>();

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "{timeInterval=%s,resolution=%s,Point=%s}", timeInterval, resolution,
                    points);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class TimeInterval {
        @JsonProperty("start")
        public String start = "";

        @JsonProperty("end")
        public String end = "";

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%s-%s", start, end);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Point {
        @JsonProperty("position")
        public int position = 0;

        @JsonProperty("quantity")
        public int quantity = 0;

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%d:%d", position, quantity);
        }

    }

}
