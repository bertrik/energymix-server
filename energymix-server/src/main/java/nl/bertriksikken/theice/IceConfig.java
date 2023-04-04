package nl.bertriksikken.theice;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import nl.bertriksikken.energymix.app.RestApiConfig;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public final class IceConfig extends RestApiConfig {

    // no-arg jackson constructor
    public IceConfig() {
        super("https://www.theice.com", 30);
    }

}
