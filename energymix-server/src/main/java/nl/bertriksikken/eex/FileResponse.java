package nl.bertriksikken.eex;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public final class FileResponse {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

    private final String contents;
    private final Instant lastModified;

    FileResponse(String contents, Instant lastModified) {
        this.contents = contents;
        this.lastModified = lastModified;
    }

    public static FileResponse create(String body, String lastModified) {
        Instant date = DATE_TIME_FORMATTER.parse(lastModified, Instant::from);
        return new FileResponse(body.replace("\ufeff", ""), date);
    }

    public static FileResponse empty() {
        return new FileResponse("", Instant.now());
    }

    public String getContents() {
        return contents;
    }

    public Instant getLastModified() {
        return lastModified;
    }

}
