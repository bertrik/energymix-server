package nl.bertriksikken.entsoe;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

public final class EntsoeResponseTest {

    @Test
    public void testDeserialize() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("A75_actualgeneration.xml");
        XmlMapper mapper = new XmlMapper();
        EntsoeResponse document = mapper.readValue(is, EntsoeResponse.class);
        System.out.println(document);
    }

}
