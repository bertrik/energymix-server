package nl.bertriksikken.entsoe;

import com.google.common.collect.Iterables;
import nl.bertriksikken.entsoe.EntsoeResponse.Period;
import nl.bertriksikken.entsoe.EntsoeResponse.Point;
import nl.bertriksikken.entsoe.EntsoeResponse.TimeSeries;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class EntsoeParser {

    private final EntsoeResponse document;

    public EntsoeParser(EntsoeResponse document) {
        this.document = Objects.requireNonNull(document);
    }

    public Double findDayAheadPrice(Instant time) {
        return parseDayAheadPrices().stream().filter(r -> r.match(time)).map(r -> r.value).findFirst()
                .orElse(Double.NaN);
    }

    public List<Result> parseDayAheadPrices() {
        List<Result> prices = new ArrayList<>();
        for (TimeSeries timeSeries : document.timeSeries()) {
            for (Period period : timeSeries.period()) {
                Duration resolution = Duration.parse(period.resolution());
                for (Point point : period.points()) {
                    Instant blockEnd = period.timeInterval().getStart().plus(resolution.multipliedBy(point.position()));
                    Instant blockStart = blockEnd.minus(resolution);
                    prices.add(new Result(blockStart, blockEnd, point.priceAmount()));
                }
            }
        }
        return prices;
    }

    public Result findByTime(Instant time, EPsrType psrType) {
        for (TimeSeries timeSeries : document.timeSeries()) {
            if (timeSeries.psrType().psrType() == psrType) {
                for (Period period : timeSeries.period()) {
                    Duration resolution = Duration.parse(period.resolution());
                    for (Point point : period.points()) {
                        Instant blockEnd = period.timeInterval().getStart().plus(resolution.multipliedBy(point.position()));
                        Instant blockStart = blockEnd.minus(resolution);
                        Result result = new Result(blockStart, blockEnd, point.quantity());
                        if (result.match(time)) {
                            return result;
                        }
                    }
                }
            }
        }
        return new Result(time, time, Double.NaN);
    }

    // find most recent generation result for specified type, null if not found
    public Result findMostRecentGeneration(EPsrType psrType) {
        List<Result> results = new ArrayList<>();
        for (TimeSeries timeSeries : document.timeSeries()) {
            if (timeSeries.isGeneration() && (timeSeries.psrType().psrType() == psrType)) {
                for (Period period : timeSeries.period()) {
                    Duration resolution = Duration.parse(period.resolution());
                    for (Point point : period.points()) {
                        Instant blockEnd = period.timeInterval().getStart().plus(resolution.multipliedBy(point.position()));
                        Instant blockStart = blockEnd.minus(resolution);
                        results.add(new Result(blockStart, blockEnd, point.quantity()));
                    }
                }
            }
        }
        return Iterables.getLast(results, null);
    }

    public Map<EPsrType, Integer> parseInstalledCapacity() {
        Map<EPsrType, Integer> map = new LinkedHashMap<>();
        for (TimeSeries timeSeries : document.timeSeries()) {
            for (Period period : timeSeries.period()) {
                for (Point point : period.points()) {
                    map.put(timeSeries.psrType().psrType(), point.quantity());
                }
            }
        }
        return map;
    }

    // parse result
    public static final class Result {
        public final Instant timeBegin;
        public final Instant timeEnd;
        public final double value;

        public Result(Instant timeBegin, Instant timeEnd, double value) {
            this.timeBegin = Objects.requireNonNull(timeBegin);
            this.timeEnd = Objects.requireNonNull(timeEnd);
            this.value = value;
        }

        public boolean match(Instant time) {
            // begin time inclusive, end time exclusive
            return !time.isBefore(timeBegin) && time.isBefore(timeEnd);
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%.0f @ %s-%s", value, timeBegin, timeEnd);
        }
    }

}
