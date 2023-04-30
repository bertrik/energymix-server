package nl.bertriksikken.entsog;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EntsogRequest {

    private final Map<String, String> params = new LinkedHashMap<>();

    public EntsogRequest(EOperatorKey operatorKey, EIndicator... indicators) {
        params.put("operatorKey", operatorKey.getKey());
        String indicator = Stream.of(indicators).map(EIndicator::getCode).collect(Collectors.joining(","));
        params.put("indicator", indicator);
    }

    public void setPeriod(ZoneId zoneId, EPeriodType periodType, Instant from, Instant to) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(zoneId);
        params.put("timezone", zoneId.getId());
        params.put("periodType", periodType.getCode());
        params.put("from", formatter.format(from));
        params.put("to", formatter.format(to));
    }

    public void setDirection(EDirectionKey direction) {
        params.put("directionKey", direction.getKey());
    }

    public Map<String, String> getParams() {
        return Map.copyOf(params);
    }

    @Override
    public String toString() {
        return params.toString();
    }
}
