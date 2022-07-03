package nl.bertriksikken.energymix.app;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Preconditions;

import nl.bertriksikken.berthub.BerthubFetcher;

public class BerthubFetcherHealthCheck extends HealthCheck {
    
    private final BerthubFetcher fetcher;

    BerthubFetcherHealthCheck(BerthubFetcher fetcher) {
        this.fetcher = Preconditions.checkNotNull(fetcher);
    }

    @Override
    protected Result check() throws Exception {
        if (!fetcher.isHealthy()) {
            return Result.unhealthy("not feeling well");
        }
        return Result.healthy();
    }
    

}
