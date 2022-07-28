package nl.bertriksikken.entsoe;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public final class EntsoeRequest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
            .withZone(ZoneOffset.UTC);

    private final Map<String, String> params = new HashMap<>();

    public EntsoeRequest(EDocumentType documentType) {
        params.put("documentType", documentType.getCode());
    }

    public void setProcessType(EProcessType processType) {
        params.put("processType", processType.getCode());
    }
    
    public void setProductionType(EPsrType psrType) {
        params.put("psrType", psrType.getCode());
    }

    public void setInDomain(EArea area) {
        params.put("in_Domain", area.getCode());
    }
    
    public void setOutDomain(EArea area) {
        params.put("out_Domain", area.getCode());
    }
    
    public void setPeriod(Instant periodStart, Instant periodEnd) {
        params.put("periodStart", FORMATTER.format(periodStart));
        params.put("periodEnd", FORMATTER.format(periodEnd));
    }

    public Map<String, String> getParams() {
        return ImmutableMap.copyOf(params);
    }

}
