package nl.bertriksikken.energymix.entsoe;

import java.io.IOException;

public final class EntsoeClientException extends IOException {

    private static final long serialVersionUID = 1L;

    public EntsoeClientException(String string) {
        super(string);
    }

}
