package nl.bertriksikken.naturalgas;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import nl.bertriksikken.entsog.EAdjacentSystemsKey;
import nl.bertriksikken.entsog.EIndicator;
import nl.bertriksikken.entsog.EntsogAggregatedData;
import nl.bertriksikken.entsog.EntsogAggregatedData.AggregatedData;

/**
 * Builds a GasFlows structure.
 */
public final class GasFlowsFactory {

    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter timeFormatter;

    public GasFlowsFactory(ZoneId zoneId) {
        this.dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(zoneId);
        this.timeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zoneId);
    }

    public GasFlows build(EntsogAggregatedData entsogAggregatedData) {
        // calculate date and last-updated
        LocalDate date = LocalDate.MIN;
        OffsetDateTime lastUpdated = OffsetDateTime.MIN;
        for (AggregatedData data : entsogAggregatedData.aggregatedData) {
            if (data.lastUpdateDateTime.isAfter(lastUpdated)) {
                lastUpdated = data.lastUpdateDateTime;
                date = data.getDate();
            }
        }
        // build structure
        GasFlows gasFlows = new GasFlows(dateFormatter.format(date), timeFormatter.format(lastUpdated));
        for (AggregatedData data : entsogAggregatedData.aggregatedData) {
            if ((data.adjacentSystemsKey != EAdjacentSystemsKey.UNKNOWN)
                    && (data.indicator == EIndicator.PHYSICAL_FLOW)) {
                String updated = timeFormatter.format(data.lastUpdateDateTime);
                int mwh = (int) Math.round(data.value / 1000);
                gasFlows.addFlow(updated, data.adjacentSystemsKey, data.directionKey, mwh);
            }
        }
        return gasFlows;
    }

    public GasFlows copy(GasFlows original) {
        GasFlows gasFlows = new GasFlows(original.date, original.lastUpdated);
        original.flows.forEach(gasFlows.flows::add);
        return gasFlows;
    }

}
