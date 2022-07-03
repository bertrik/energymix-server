package nl.bertriksikken.berthub;

import java.io.IOException;
import java.time.Instant;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;

public final class BerthubFetcherTest {
    
    public static void main(String[] args) throws IOException {
        CsvMapper csvMapper = new CsvMapper();
        BerthubFetcherConfig config = new BerthubFetcherConfig();
        BerthubFetcher fetcher = BerthubFetcher.create(config);
        Instant now = Instant.now();
        String csv = fetcher.download(now);
        ProductionDataCsv all = ProductionDataCsv.parse(csvMapper, csv);
        ProductionData latest = all.getLatest();
        
        System.out.println(latest);
    }

}
