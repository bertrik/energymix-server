package nl.bertriksikken.eex;

import org.junit.Assert;
import org.junit.Test;

public final class FileResponseTest {

    @Test
    public void testParseDate() {
        FileResponse response = FileResponse.create("content", "Fri, 28 Oct 2022 13:31:03 GMT");
        Assert.assertEquals("content", response.getContents());
        Assert.assertNotNull(response.getLastModified());
    }

    @Test
    public void testEmpty() {
        FileResponse response = FileResponse.empty();
        Assert.assertNotNull(response.getContents());
        Assert.assertNotNull(response.getLastModified());
    }

}
