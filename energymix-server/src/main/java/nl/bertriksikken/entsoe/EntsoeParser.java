package nl.bertriksikken.entsoe;

import java.time.Duration;
import java.time.Instant;

import com.google.common.base.Preconditions;

import nl.bertriksikken.entsoe.EntsoeResponse.Period;
import nl.bertriksikken.entsoe.EntsoeResponse.Point;
import nl.bertriksikken.entsoe.EntsoeResponse.TimeInterval;
import nl.bertriksikken.entsoe.EntsoeResponse.TimeSeries;

public final class EntsoeParser {

    private final EntsoeResponse document;

    public EntsoeParser(EntsoeResponse document) {
        this.document = Preconditions.checkNotNull(document);
    }

    public Double findPoint(Instant now, EPsrType psrType) {
        for (TimeSeries timeSeries : document.timeSeries) {
            if (timeSeries.psrType.psrType == psrType) {
                for (Period period : timeSeries.period) {
                    Duration resolution = Duration.parse(period.resolution);
                    TimeInterval interval = period.timeInterval;
                    Instant intervalStart = interval.getStart();
                    for (Point point : period.points) {
                        Instant intervalEnd = intervalStart.plus(resolution);
                        if (isBetween(now, intervalStart, intervalEnd)) {
                            return Double.valueOf(point.quantity);
                        }
                        intervalStart = intervalEnd;
                    }
                }
            }
        }
        return Double.NaN;
    }

    private boolean isBetween(Instant time, Instant start, Instant end) {
        return !time.isBefore(start) && time.isBefore(end);
    }
}
