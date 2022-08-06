package nl.bertriksikken.entsoe;

import java.time.Instant;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public final class EntsoeRequestTest {

    @Test
    public void testBuildRequest() {
        EntsoeRequest request = new EntsoeRequest(EDocumentType.PRICE_DOCUMENT);
        request.setInDomain("indomain");
        request.setOutDomain("outdomain");
        request.setPeriod(Instant.now(), Instant.now());
        request.setProcessType(EProcessType.DAY_AHEAD);
        request.setProductionType(EPsrType.FOSSIL_GAS);

        Map<String, String> params = request.getParams();

        Assert.assertEquals("indomain", params.get("in_Domain"));
        Assert.assertEquals("outdomain", params.get("out_Domain"));
        Assert.assertEquals("A01", params.get("processType"));
        Assert.assertEquals("B04", params.get("psrType"));
    }

}
