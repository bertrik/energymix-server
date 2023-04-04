package nl.bertriksikken.eex;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import nl.bertriksikken.energymix.app.RestApiConfig;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public final class EexConfig extends RestApiConfig {

    // no-arg jackson constructor
    public EexConfig() {
        super("https://gasandregistry.eex.com", 30);
    }

}
