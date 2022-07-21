package nl.bertriksikken.entsoe;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import nl.bertriksikken.entsoe.EntsoeResponse.Period;
import nl.bertriksikken.entsoe.EntsoeResponse.Point;
import nl.bertriksikken.entsoe.EntsoeResponse.TimeSeries;

public final class EntsoeParser {

    private final EntsoeResponse document;

    public EntsoeParser(EntsoeResponse document) {
        this.document = Preconditions.checkNotNull(document);
    }

    public Result findByTime(Instant time, EPsrType psrType) {
        for (TimeSeries timeSeries : document.timeSeries) {
            if (timeSeries.psrType.psrType == psrType) {
                for (Period period : timeSeries.period) {
                    Duration resolution = Duration.parse(period.resolution);
                    for (Point point : period.points) {
                        Instant blockEnd = period.timeInterval.getStart().plus(resolution.multipliedBy(point.position));
                        Instant blockStart = blockEnd.minus(resolution);
                        if (isBetween(time, blockStart, blockEnd)) {
                            return new Result(blockEnd, point.quantity);
                        }
                    }
                }
            }
        }
        return new Result(Instant.now(), Double.NaN);
    }

    public Result findMostRecentGeneration(EPsrType psrType) {
        for (TimeSeries timeSeries : document.timeSeries) {
            if (timeSeries.isGeneration() && (timeSeries.psrType.psrType == psrType)) {
                Period lastPeriod = Iterables.getLast(timeSeries.period, null);
                if (lastPeriod != null) {
                    Duration resolution = Duration.parse(lastPeriod.resolution);
                    Point point = Iterables.getLast(lastPeriod.points);
                    Instant blockEnd = lastPeriod.timeInterval.getStart().plus(resolution.multipliedBy(point.position));
                    return new Result(blockEnd, point.quantity);
                }
            }
        }
        return new Result(Instant.now(), Double.NaN);
    }

    // from begin (exclusive) to end (inclusive)
    private boolean isBetween(Instant time, Instant start, Instant end) {
        return time.isAfter(start) && !time.isAfter(end);
    }

    // parse result
    public static final class Result {
        public final Instant time;
        public final double value;

        public Result(Instant time, double quantity) {
            this.time = time;
            this.value = quantity;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "{%5.0f@%s}", value, time);
        }
    }
}
