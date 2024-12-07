package nl.bertriksikken.energymix.ned;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.Iterables;
import nl.bertriksikken.ned.EEnergyType;
import nl.bertriksikken.ned.EGranularity;
import nl.bertriksikken.ned.UtilizationJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;

public final class NednlWindTester {

    private static final Logger LOG = LoggerFactory.getLogger(NednlWindTester.class);
    private static final CsvMapper CSV_MAPPER = new CsvMapper();

    public static void main(String[] args) throws IOException {
        NednlWindTester tester = new NednlWindTester();
        tester.run();
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

    private void run() throws IOException {
        NedConfig config = readConfig(new File(".nednl.yaml"));
        File onshoreFile = new File("onshore.csv");
        File offshoreFile = new File("offshore.csv");

        List<UtilizationJson> list;
        try (NedClient client = NedClient.create(config)) {
            for (int i = 0; i < 60; i++) {
                Instant now = Instant.now();
                list = client.getUtilizations(now, EEnergyType.WIND, EGranularity.FIFTEEN_MINUTES);
                UtilizationJson windOnShore = Iterables.getLast(list);
                writeCsvRow(onshoreFile, windOnShore);

                list = client.getUtilizations(now, EEnergyType.WIND_OFFSHORE_C, EGranularity.FIFTEEN_MINUTES);
                UtilizationJson windOffshore = Iterables.getLast(list);
                writeCsvRow(offshoreFile, windOffshore);

                try {
                    Thread.sleep(60_000);
                } catch (InterruptedException e) {
                    LOG.info("Caught InterruptedException: {}", e.getMessage());
                }
            }
        }
    }

    private void writeCsvRow(File file, UtilizationJson json) throws IOException {
        LOG.info("Writing to {}: {}", file.getName(), json);
        boolean append = file.exists();
        CsvSchema schema = CSV_MAPPER.schemaFor(UtilizationJson.class);
        schema = append ? schema.withoutHeader() : schema.withHeader();
        ObjectWriter writer = CSV_MAPPER.writer(schema);
        try (BufferedWriter fileWriter = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8,
                append ? StandardOpenOption.APPEND : StandardOpenOption.CREATE)) {
            writer.writeValue(fileWriter, json);
        }
    }

}
