package nl.bertriksikken.energymix.ned;

import com.google.common.collect.Iterables;
import nl.bertriksikken.energymix.server.CatchingRunnable;
import nl.bertriksikken.energymix.server.EnergyMix;
import nl.bertriksikken.energymix.server.EnergyMixFactory;
import nl.bertriksikken.ned.EEnergyType;
import nl.bertriksikken.ned.EGranularity;
import nl.bertriksikken.ned.UtilizationJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class NedHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NedHandler.class);
    private static final ZoneId TIME_ZONE = ZoneId.of("Europe/Amsterdam");

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Map<EEnergyType, UtilizationJson> utilizationMap = new ConcurrentHashMap<>();
    private final NedConfig config;
    private final NedClient client;

    public NedHandler(NedConfig config) {
        this.config = Objects.requireNonNull(config);
        this.client = NedClient.create(config);
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    public void start() {
        executor.scheduleAtFixedRate(new CatchingRunnable(LOG, this::downloadGeneration), 0, config.getInterval().toSeconds(), TimeUnit.SECONDS);
    }

    public void stop() {
        client.close();
        executor.shutdownNow();
    }

    private void downloadGeneration() throws IOException {
        Instant now = Instant.now();
        LOG.info("Downloading from ned.nl ...");
        List<EEnergyType> types = List.of(EEnergyType.SOLAR, EEnergyType.WIND, EEnergyType.WIND_OFFSHORE_C,
                EEnergyType.FOSSIL_GAS_POWER, EEnergyType.FOSSIL_HARD_COAL, EEnergyType.BIO_MASS,
                EEnergyType.NUCLEAR, EEnergyType.WASTE_POWER, EEnergyType.OTHER_POWER);
        for (EEnergyType type : types) {
            List<UtilizationJson> list = client.getUtilizations(now, type, EGranularity.FIFTEEN_MINUTES);
            if (!list.isEmpty()) {
                UtilizationJson mostRecent = Iterables.getLast(list);
                utilizationMap.put(type, mostRecent);
            }
        }
        Duration duration = Duration.between(now, Instant.now());
        LOG.info("Download from ned.nl took {} ms", duration.toMillis());

        EnergyMix energyMix = getGeneration();
        LOG.info("NED energy mix: {}", energyMix);
    }

    private double getPower(EEnergyType type) {
        UtilizationJson utilization = utilizationMap.get(type);
        if (utilization == null) {
            return Double.NaN;
        }
        // generation power is called 'capacity' in NED terminology
        return utilization.capacity / 1E3;
    }

    public EnergyMix getGeneration() {
        UtilizationJson fossilGasUtilization = utilizationMap.get(EEnergyType.FOSSIL_GAS_POWER);
        Instant instant = Instant.parse(fossilGasUtilization.lastUpdate);

        EnergyMixFactory factory = new EnergyMixFactory(TIME_ZONE);
        EnergyMix energyMix = factory.build(instant);
        energyMix.addComponent("solar", getPower(EEnergyType.SOLAR), "#FFFF00");
        energyMix.addComponent("wind onshore", getPower(EEnergyType.WIND), "#0000FF");
        energyMix.addComponent("wind offshore", getPower(EEnergyType.WIND_OFFSHORE_C), "#0000FF");
        energyMix.addComponent("fossil gas", getPower(EEnergyType.FOSSIL_GAS_POWER), "#FF0000");
        energyMix.addComponent("fossil coal", getPower(EEnergyType.FOSSIL_HARD_COAL), "#FF0000");
        energyMix.addComponent("biomass", getPower(EEnergyType.BIO_MASS), "#FF0000");
        energyMix.addComponent("nuclear", getPower(EEnergyType.NUCLEAR), "#00FF00");
        energyMix.addComponent("waste", getPower(EEnergyType.WASTE_POWER), "#FF00FF");
        energyMix.addComponent("other", getPower(EEnergyType.OTHER_POWER), "#FF00FF");
        return energyMix;
    }
}
