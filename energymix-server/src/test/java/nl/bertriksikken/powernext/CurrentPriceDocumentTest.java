package nl.bertriksikken.powernext;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import nl.bertriksikken.naturalgas.NeutralGasPrices;
import nl.bertriksikken.naturalgas.NeutralGasPrices.NeutralGasDayPrice;
import nl.bertriksikken.naturalgas.NeutralGasPrices.NeutralGasDayPrice.ENgpStatus;

public final class CurrentPriceDocumentTest {

    private static final Logger LOG = LoggerFactory.getLogger(CurrentPriceDocumentTest.class);

    @Test
    public void testPriceDocument() throws IOException {
        String date = "Fri, 28 Oct 2022 17:16:04 GMT";
        URL url = Resources.getResource("NGP-Current-Prices.csv");
        FileResponse response = FileResponse.create(Resources.toString(url, StandardCharsets.UTF_8), date);

        NeutralGasPrices document = CurrentPriceDocument.parse(response);
        Assert.assertNotNull(document);
        LOG.info("Document: {}", document);

        NeutralGasDayPrice finalPrice = document.findFinalPrice();
        Assert.assertEquals(ENgpStatus.FINAL, finalPrice.status);
        Assert.assertEquals(29.937, finalPrice.indexValue, 0.001);
        
        Assert.assertEquals(2, document.getTemporaryPrices().size());
    }

}
