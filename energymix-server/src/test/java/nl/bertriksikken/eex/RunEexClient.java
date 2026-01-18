package nl.bertriksikken.eex;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class RunEexClient {

    public static void main(String[] args) throws IOException {
        RunEexClient runner = new RunEexClient();
        runner.run();
    }

    private void run() throws IOException {
        EexConfig config = new EexConfig();
        try (EexClient client = EexClient.create(config)) {
            FileResponse response = client.getCurrentPriceDocument();
            try (FileOutputStream fos = new FileOutputStream(CurrentPriceDocument.NGP_CURRENT_PRICES)) {
                fos.write(response.getContents().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

}
