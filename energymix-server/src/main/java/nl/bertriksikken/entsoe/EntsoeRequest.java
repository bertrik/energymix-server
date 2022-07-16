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

    public EntsoeRequest(EDocumentType documentType, EProcessType processType, EArea inDomain, Instant periodStart,
            Instant periodEnd) {
        params.put("documentType", documentType.getCode());
        params.put("processType", processType.getCode());
        params.put("in_Domain", inDomain.getCode());
        params.put("periodStart", FORMATTER.format(periodStart));
        params.put("periodEnd", FORMATTER.format(periodEnd));
    }

    public void setProductionType(EPsrType psrType) {
        params.put("psrType", psrType.getCode());
    }

    public Map<String, String> getParams() {
        return ImmutableMap.copyOf(params);
    }

}
