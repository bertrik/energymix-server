package nl.bertriksikken.entsoe;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public final class EntsoeResponseTest {

    @Test
    public void testDeserialize() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("actual.xml");
        XmlMapper mapper = new XmlMapper();
        EntsoeResponse document = mapper.readValue(is, EntsoeResponse.class);
        System.out.println(document);
    }
    
}
