package nl.bertriksikken.eex;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class FileResponseTest {

    @Test
    public void testParseDate() {
        FileResponse response = FileResponse.create("content", "Fri, 28 Oct 2022 13:31:03 GMT");
        Assertions.assertEquals("content", response.getContents());
        Assertions.assertNotNull(response.getLastModified());
    }

    @Test
    public void testEmpty() {
        FileResponse response = FileResponse.empty();
        Assertions.assertNotNull(response.getContents());
        Assertions.assertNotNull(response.getLastModified());
    }

}
