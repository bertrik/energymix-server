package nl.bertriksikken.entsoe;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public final class EPsrTypeTest {

    /**
     * Verifies that all PSR types have a unique code.
     */
    @Test
    public void testUniqueCode() {
        Map<String, EPsrType> codes = new HashMap<>();
        for (EPsrType psrType : EPsrType.values()) {
            Assert.assertNull(codes.put(psrType.getCode(), psrType));
        }
    }

}
