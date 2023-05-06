package nl.bertriksikken.energymix.server;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class EnergyMixFactory {

    private final DateTimeFormatter dateTimeFormatter;

    public EnergyMixFactory(ZoneId zoneId) {
        dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zoneId);
    }

    public EnergyMix build(Instant instant) {
        String dateTime = dateTimeFormatter.format(instant);
        return new EnergyMix(instant.getEpochSecond(), dateTime);
    }

}
