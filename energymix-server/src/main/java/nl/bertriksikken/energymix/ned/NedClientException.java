package nl.bertriksikken.energymix.ned;

import java.io.IOException;

public final class NedClientException extends IOException {

    public NedClientException(String message) {
        super(message);
    }
}
