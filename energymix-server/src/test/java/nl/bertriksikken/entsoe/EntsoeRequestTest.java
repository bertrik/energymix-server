package nl.bertriksikken.entsoe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

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

        Assertions.assertEquals("indomain", params.get("in_Domain"));
        Assertions.assertEquals("outdomain", params.get("out_Domain"));
        Assertions.assertEquals("A01", params.get("processType"));
        Assertions.assertEquals("B04", params.get("psrType"));
    }

}
