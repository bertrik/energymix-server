package nl.bertriksikken.energymix.ned;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.Iterables;
import nl.bertriksikken.energymix.server.EnergyMix;
import nl.bertriksikken.energymix.server.EnergyMixFactory;
import nl.bertriksikken.ned.EEnergyType;
import nl.bertriksikken.ned.EGranularity;
import nl.bertriksikken.ned.UtilizationJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RunNednlClient {

    private static final Logger LOG = LoggerFactory.getLogger(RunNednlClient.class);

    public static void main(String[] args) {
        RunNednlClient runner = new RunNednlClient();
        try {
            runner.run();
        } catch (IOException e) {
            LOG.error("Caught IOException", e);
        }
    }

    private void run() throws IOException {
        NedConfig config = readConfig(new File(".nednl.yaml"));
        NedClient client = NedClient.create(config);
        List<EEnergyType> types = List.of(EEnergyType.SOLAR, EEnergyType.WIND, EEnergyType.WIND_OFFSHORE_C,
                EEnergyType.FOSSIL_GAS_POWER, EEnergyType.FOSSIL_HARD_COAL, EEnergyType.NUCLEAR, EEnergyType.WASTE_POWER, EEnergyType.OTHER_POWER);
        Instant now = Instant.now();
        Map<EEnergyType, UtilizationJson> map = new HashMap<>();
        try {
            for (EEnergyType type : types) {
                List<UtilizationJson> list = client.getUtilizations(now, type, EGranularity.FIFTEEN_MINUTES);
                if (!list.isEmpty()) {
                    UtilizationJson mostRecent = Iterables.getLast(list);
                    map.put(type, mostRecent);
                }
            }
        } finally {
            client.shutdown();
        }

        UtilizationJson fossilGasUtilization = map.get(EEnergyType.FOSSIL_GAS_POWER);
        Instant instant = Instant.parse(fossilGasUtilization.lastUpdate);

        // build energy mix structure
        EnergyMixFactory energyMixFactory = new EnergyMixFactory(ZoneId.of("Europe/Amsterdam"));
        EnergyMix energyMix = energyMixFactory.build(instant);

        double solar = getPower(map, EEnergyType.SOLAR);
        energyMix.addComponent("solar", solar, "#FFFF00");
        double windOnshore = getPower(map, EEnergyType.WIND);
        energyMix.addComponent("wind onshore", windOnshore, "#0000FF");
        double windOffshore = getPower(map, EEnergyType.WIND_OFFSHORE_C);
        energyMix.addComponent("wind offshore", windOffshore, "#0000FF");
        double fossilGas = getPower(map, EEnergyType.FOSSIL_GAS_POWER);
        energyMix.addComponent("fossil gas", fossilGas, "#FF0000");
        double fossilCoal = getPower(map, EEnergyType.FOSSIL_HARD_COAL);
        energyMix.addComponent("fossil coal", fossilCoal, "#FF0000");
        double nuclear = getPower(map, EEnergyType.NUCLEAR);
        energyMix.addComponent("nuclear", nuclear, "#00FF00");
        double waste = getPower(map, EEnergyType.WASTE_POWER);
        energyMix.addComponent("waste", waste, "#FF00FF");
        double other = getPower(map, EEnergyType.OTHER_POWER);
        energyMix.addComponent("other", other, "#FF00FF");
        ObjectMapper mapper = new ObjectMapper();
        String string = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(energyMix);
        LOG.info("Energy mix is now: {}", string);
    }

    private double getPower(Map<EEnergyType, UtilizationJson> map, EEnergyType type) {
        UtilizationJson utilization = map.get(type);
        if (utilization == null) {
            return Double.NaN;
        }
        return 4 * utilization.volume / 1E3;
    }

    private NedConfig readConfig(File file) throws IOException {
        YAMLMapper yamlMapper = new YAMLMapper();
        NedConfig config = new NedConfig();
        if (file.exists()) {
            config = yamlMapper.readValue(file, NedConfig.class);
        } else {
            yamlMapper.writeValue(file, config);
        }
        return config;
    }

}
