package nl.bertriksikken.berthub;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

public final class ProductionDataCsvTest {

    @Test
    public void test() throws IOException {
        CsvMapper csvMapper = new CsvMapper();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("nlprod-harvested.csv");
        String csvString = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
        ProductionDataCsv csv = ProductionDataCsv.parse(csvMapper, csvString);
        ProductionData latest = csv.getLatest();
        System.out.println(latest);
        
        Assert.assertEquals(911, latest.wind, 0.1);
    }

}
