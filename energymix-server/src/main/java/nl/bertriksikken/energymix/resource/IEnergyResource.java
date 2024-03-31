package nl.bertriksikken.energymix.resource;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(
        info = @Info(title = "Open energy data",
                description = "Open energy data API," +
                        " with electricity data from ENTSO-E," +
                        " natural gas data from ENTSO-G and public market data",
                contact = @Contact(name = "Bertrik Sikken", email = "bertrik@gmail.com")),
        servers = {@Server(url = "https://stofradar.nl"), @Server(url = "http://stofradar.nl:9001")},
        tags = {@Tag(name = "electricity", description = "Electricity related data"),
                @Tag(name = "naturalgas", description = "Natural gas related data")}
)
public interface IEnergyResource {
}
