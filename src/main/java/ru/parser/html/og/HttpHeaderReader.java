package ru.parser.html.og;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class HttpHeaderReader {

    private static final String ENCLOSING_HEAD = "</head>";

    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public CompletableFuture<String> readHeader(String link) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(link))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "text/html")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(httpResponse -> {
                    try {
                        return readHeader(httpResponse);
                    } catch (Exception ex) {
                        throw new CompletionException(ex);
                    }
                });
    }

    private String readHeader(HttpResponse<InputStream> httpResponse) throws IOException {
        StringBuilder header = new StringBuilder();
        try (InputStream inputStream = httpResponse.body()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            for (String line; (line = reader.readLine()) != null; ) {
                int enclosingHeadIndex = line.indexOf(ENCLOSING_HEAD);
                if (enclosingHeadIndex > 0) {
                    header.append(line, 0, enclosingHeadIndex + ENCLOSING_HEAD.length());
                    header.append("<body></body></html>");
                    break;
                } else {
                    header.append(line);
                }
            }
        }
        return header.toString();
    }
}
