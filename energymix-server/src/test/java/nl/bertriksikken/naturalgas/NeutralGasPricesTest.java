package nl.bertriksikken.naturalgas;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.bertriksikken.naturalgas.NeutralGasPrices.NeutralGasDayPrice;
import nl.bertriksikken.naturalgas.NeutralGasPrices.NeutralGasDayPrice.ENgpStatus;

public final class NeutralGasPricesTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(NeutralGasPricesTest.class);

    @Test
    public void testToString() {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        NeutralGasPrices prices = new NeutralGasPrices(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        prices.add(new NeutralGasDayPrice(today.minusDays(1), 0.50, 1000, ENgpStatus.FINAL, now));
        prices.add(new NeutralGasDayPrice(today, 0.60, 1000, ENgpStatus.TEMPORARY, now));
        prices.add(new NeutralGasDayPrice(today.plusDays(1), 0.70, 1000, ENgpStatus.TEMPORARY, now));
        prices.add(new NeutralGasDayPrice(today.minusDays(1), 0, 0, ENgpStatus.TEMPORARY, now));
        
        String s = prices.toString();
        LOG.info(s);
    }
    
}
