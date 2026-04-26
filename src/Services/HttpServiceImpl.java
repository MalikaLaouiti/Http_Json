package Services;


import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;

public class HttpServiceImpl implements IHTTPService {

    private final HttpClient client;

    public HttpServiceImpl() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String get(String url, JsonObject headers) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET();
        applyHeaders(builder, headers);
        return send(builder.build());
    }

    @Override
    public String post(String url, String body, JsonObject headers) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(body));
        applyHeaders(builder, headers);
        return send(builder.build());
    }

    @Override
    public String put(String url, String body, JsonObject headers) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .PUT(HttpRequest.BodyPublishers.ofString(body));
        applyHeaders(builder, headers);
        return send(builder.build());
    }

    @Override
    public String patch(String url, String body, JsonObject headers) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body));
        applyHeaders(builder, headers);
        return send(builder.build());
    }

    @Override
    public String delete(String url, JsonObject headers) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .DELETE();
        applyHeaders(builder, headers);
        return send(builder.build());
    }


    private void applyHeaders(HttpRequest.Builder builder, JsonObject headers) {
        // Content-Type par défaut pour les requêtes avec body
        builder.header("Content-Type", "application/json");

        if (headers != null) {
            Iterator<String> keys = headers.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                builder.header(key, headers.getString(key));
            }
        }
    }

    private String send(HttpRequest request) throws Exception {
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        String rawBody = response.body();
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("http_status", response.statusCode());

        // Tente de parser le body comme JSON
        try (JsonReader reader = Json.createReader(new StringReader(rawBody))) {
            builder.add("body", reader.read());
        } catch (Exception e) {
            // Body non-JSON (HTML, texte...)
            builder.add("body", rawBody);
        }

        return Json.createObjectBuilder()
                .add("status", "OK")
                .add("data", builder.build())
                .build().toString();
    }
}
