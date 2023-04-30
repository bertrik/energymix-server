package nl.bertriksikken.energymix.entsog;

import java.io.IOException;

public final class EntsogClientException extends IOException {

    private static final long serialVersionUID = 1L;

    public EntsogClientException(String string) {
        super(string);
    }

}
