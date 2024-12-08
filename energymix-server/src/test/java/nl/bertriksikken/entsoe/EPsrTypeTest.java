package nl.bertriksikken.entsoe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public final class EPsrTypeTest {

    /**
     * Verifies that all PSR types have a unique code.
     */
    @Test
    public void testUniqueCode() {
        Map<String, EPsrType> codes = new HashMap<>();
        for (EPsrType psrType : EPsrType.values()) {
            Assertions.assertNull(codes.put(psrType.getCode(), psrType));
        }
    }

}
