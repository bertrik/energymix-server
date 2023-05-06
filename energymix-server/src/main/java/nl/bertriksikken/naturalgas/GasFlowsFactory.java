package nl.bertriksikken.naturalgas;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.google.common.collect.Iterables;

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
        AggregatedData first = Iterables.getFirst(entsogAggregatedData.aggregatedData, null);
        String date = dateFormatter.format(first.getDate());
        String lastUpdated = timeFormatter.format(first.lastUpdateDateTime);
        GasFlows gasFlows = new GasFlows(date, lastUpdated);
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

}
