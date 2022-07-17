package nl.bertriksikken.entsoe;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public final class EntsoeParserTest {

    @Test
    public void testExtractSolar() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("A69_solar_wind_forecast.xml");
        XmlMapper mapper = new XmlMapper();
        EntsoeResponse document = mapper.readValue(is, EntsoeResponse.class);

        // extract and verify
        Instant now = Instant.parse("2022-07-17T12:34:00Z");
        EntsoeParser parser = new EntsoeParser(document);
        Double value = parser.findPoint(now, EPsrType.SOLAR);
        Assert.assertEquals(6248.0, value, 0.1);
    }
}
