package nl.bertriksikken.energymix.entsoe;

import java.io.IOException;

public final class EntsoeFetcherException extends IOException {

    private static final long serialVersionUID = 1L;

    public EntsoeFetcherException(String string) {
        super(string);
    }

}
