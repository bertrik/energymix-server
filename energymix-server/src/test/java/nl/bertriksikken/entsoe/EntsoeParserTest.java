package nl.bertriksikken.entsoe;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import nl.bertriksikken.energymix.server.DayAheadPrices;
import nl.bertriksikken.entsoe.EntsoeParser.Result;

public final class EntsoeParserTest {

    private static final XmlMapper mapper = new XmlMapper();

    @Test
    public void testExtractInstalledGeneration() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("A68_installed_capacity.xml");
        EntsoeResponse document = mapper.readValue(is, EntsoeResponse.class);

        // extract and verify
        EntsoeParser parser = new EntsoeParser(document);
        Map<EPsrType, Integer> capacities = parser.parseInstalledCapacity();
        Assert.assertEquals(22590, (long) capacities.get(EPsrType.SOLAR));
    }

    @Test
    public void testExtractSolar() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("A69_solar_wind_forecast.xml");
        EntsoeResponse document = mapper.readValue(is, EntsoeResponse.class);

        // extract and verify
        Instant now = Instant.parse("2022-07-17T12:34:00Z");
        EntsoeParser parser = new EntsoeParser(document);
        Result result = parser.findByTime(now, EPsrType.SOLAR);
        Assert.assertEquals(6248.0, result.value, 0.1);
    }

    @Test
    public void testExtractGeneration() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("A68_installed_capacity.xml");
        EntsoeResponse document = mapper.readValue(is, EntsoeResponse.class);

        // extract and verify
        EntsoeParser parser = new EntsoeParser(document);
        Map<EPsrType, Integer> capacities = parser.parseInstalledCapacity();
        Assert.assertEquals(22590.0, capacities.get(EPsrType.SOLAR), 0.1);
    }

    @Test
    public void findMostRecentGeneration() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("A75_actualgeneration.xml");
        EntsoeResponse document = mapper.readValue(is, EntsoeResponse.class);

        // extract and verify
        EntsoeParser parser = new EntsoeParser(document);
        Result result = parser.findMostRecentGeneration(EPsrType.FOSSIL_HARD_COAL);
        Assert.assertEquals(3136, result.value, 0.1);
    }

    @Test
    public void parseDayAheadPrices() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("A44_day_ahead_prices.xml");
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
        String json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(prices);
        System.out.println(json);
    }

}
