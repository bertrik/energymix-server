package nl.bertriksikken.ned;

import java.time.Duration;
import java.util.stream.Stream;

public enum EGranularity {
    TEN_MINUTES(3, Duration.ofMinutes(10)), FIFTEEN_MINUTES(4, Duration.ofMinutes(15)),
    HOUR(5, Duration.ofHours(1)), DAY(6, Duration.ofDays(1)), MONTH(7, Duration.ofDays(30)),
    YEAR(8, Duration.ofDays(365));

    private final int value;
    private final Duration duration;
    private final String descriptor;

    EGranularity(int value, Duration duration) {
        this.value = value;
        this.duration = duration;
        this.descriptor = "/v1/granularities/" + value;
    }

    public int getValue() {
        return value;
    }

    public Duration getDuration() {
        return duration;
    }
    
    public static EGranularity fromDescriptor(String descriptor) {
        return Stream.of(values()).filter(g -> g.descriptor.equals(descriptor)).findFirst().orElse(null);
    }

    public enum ETimeZone {
        UTC(0), CET(1);

        private final int value;

        ETimeZone(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
