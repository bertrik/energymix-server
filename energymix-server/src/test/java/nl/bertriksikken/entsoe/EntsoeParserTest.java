package nl.bertriksikken.entsoe;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import nl.bertriksikken.energymix.server.DayAheadPrices;
import nl.bertriksikken.entsoe.EntsoeParser.Result;

public final class EntsoeParserTest {

    @Test
    public void testExtractSolar() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("A69_solar_wind_forecast.xml");
        XmlMapper mapper = new XmlMapper();
        EntsoeResponse document = mapper.readValue(is, EntsoeResponse.class);

        // extract and verify
        Instant now = Instant.parse("2022-07-17T12:34:00Z");
        EntsoeParser parser = new EntsoeParser(document);
        Result result = parser.findByTime(now, EPsrType.SOLAR);
        Assert.assertEquals(6248.0, result.value, 0.1);
    }

    @Test
    public void findMostRecentGeneration() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("A75_actualgeneration.xml");
        XmlMapper mapper = new XmlMapper();
        EntsoeResponse document = mapper.readValue(is, EntsoeResponse.class);

        // extract and verify
        EntsoeParser parser = new EntsoeParser(document);
        Result result = parser.findMostRecentGeneration(EPsrType.FOSSIL_HARD_COAL);
        Assert.assertEquals(3136, result.value, 0.1);
    }

    @Test
    public void parseDayAheadPrices() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("A44_day_ahead_prices.xml");
        XmlMapper mapper = new XmlMapper();
        EntsoeResponse document = mapper.readValue(is, EntsoeResponse.class);

        // extract and verify
        EntsoeParser parser = new EntsoeParser(document);
        List<Result> results = parser.parseDayAheadPrices();
        Assert.assertFalse(results.isEmpty());
        
        Instant time = Instant.parse("2022-07-30T22:05:00Z");
        Double currentPrice = parser.findDayAheadPrice(time);
        Assert.assertEquals(382.79, currentPrice, 0.01);
        
        // build our structure
        DayAheadPrices prices = new DayAheadPrices(time, currentPrice);
        parser.parseDayAheadPrices().forEach(r -> prices.addPrice(r.timeBegin, r.value));
        ObjectMapper jsonMapper = new ObjectMapper();
        String json = jsonMapper.writeValueAsString(prices);
        System.out.println(json);
    }

}
