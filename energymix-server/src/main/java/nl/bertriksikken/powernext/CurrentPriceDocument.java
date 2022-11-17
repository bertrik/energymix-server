package nl.bertriksikken.powernext;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import nl.bertriksikken.naturalgas.NeutralGasPrices;
import nl.bertriksikken.naturalgas.NeutralGasPrices.NeutralGasDayPrice;
import nl.bertriksikken.naturalgas.NeutralGasPrices.NeutralGasDayPrice.ENgpStatus;

/**
 * Representation of the powernext NGP current price document.
 */
public final class CurrentPriceDocument {

    private static final CsvMapper CSV_MAPPER = new CsvMapper();
    private static final CsvSchema SCHEMA = CsvSchema.emptySchema().withHeader().withColumnSeparator(';');

    private static final ZoneId ZONE = ZoneId.of("CET");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZONE);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZONE);

    // parses the CSV data into the domain model
    public static NeutralGasPrices parse(FileResponse fileResponse) throws IOException {
        NeutralGasPrices document = new NeutralGasPrices(fileResponse.getLastModified());

        // read CSV
        ObjectReader reader = CSV_MAPPER.readerFor(CurrentPriceEntryCsv.class).with(SCHEMA);
        MappingIterator<CurrentPriceEntryCsv> iterator = reader.readValues(fileResponse.getContents());
        List<CurrentPriceEntryCsv> csvEntries = iterator.readAll();

        // parse each line
        for (CurrentPriceEntryCsv csvEntry : csvEntries) {
            LocalDate date = DATE_FORMAT.parse(csvEntry.gasDay, LocalDate::from);
            EStatus status = EStatus.from(csvEntry.ngpStatus);
            Instant timestamp = TIMESTAMP_FORMAT.parse(csvEntry.timeStamp, Instant::from);
            document.add(new NeutralGasDayPrice(date, csvEntry.indexValue, csvEntry.indexVolume, status.ngpStatus,
                    timestamp));
        }
        return document;
    }

    // Representation of one line in the current price CSV.
    private static final class CurrentPriceEntryCsv {
        @JsonProperty("Gasday")
        private String gasDay = "";

        @JsonProperty("IndexValue (€/MWh)")
        private double indexValue = Double.NaN;

        @JsonProperty("IndexVolume (MWh)")
        private int indexVolume = 0;

        @JsonProperty("Status")
        private String ngpStatus = "";

        @JsonProperty("Timestamp Let")
        private String timeStamp = "";
    }

    private static enum EStatus {
        FINAL(ENgpStatus.FINAL, "Final NGP"), TEMPORARY(ENgpStatus.TEMPORARY, "Temporary NGP"),
        UNKNOWN(ENgpStatus.UNKNOWN, "Unknown");

        private final ENgpStatus ngpStatus;
        private final String description;

        EStatus(ENgpStatus ngpStatus, String description) {
            this.ngpStatus = ngpStatus;
            this.description = description;
        }

        private static EStatus from(String description) {
            return Stream.of(values()).filter(v -> v.description.equals(description)).findFirst().orElse(UNKNOWN);
        }
    }

}
