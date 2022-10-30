package nl.bertriksikken.powernext;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
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
        FileResponse response = FileResponse.create(Resources.toString(url, Charsets.UTF_8), date);

        NeutralGasPrices document = CurrentPriceDocument.parse(response);
        Assert.assertNotNull(document);
        LOG.info("Document: {}", document);

        List<NeutralGasDayPrice> dayPrices = document.getDayPrices();
        Assert.assertEquals(4, dayPrices.size());

        NeutralGasDayPrice first = dayPrices.get(0);
        Assert.assertEquals(ENgpStatus.FINAL, first.status);
        Assert.assertEquals(29.937, first.indexValue, 0.001);

        LocalDate day = LocalDate.ofInstant(document.getCreationTime(), ZoneId.of("CET")).minusDays(1);
        NeutralGasDayPrice entry = document.findDayPrice(day);
        Assert.assertNotNull(entry);
    }

}
