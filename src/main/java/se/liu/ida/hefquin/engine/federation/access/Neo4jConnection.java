package se.liu.ida.hefquin.engine.federation.access;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Neo4jConnection {
    private final HttpRequest.Builder requestBuilder;

    public Neo4jConnection(HttpRequest.Builder request) {
        this.requestBuilder = request;
    }

    public String executeQuery(String cypher) throws InterruptedException, IOException {
        String data = "{\n" +
                "  \"statements\" : [ {\n" +
                "    \"statement\" : \""+cypher+"\",\n" +
                "    \"parameters\" : {}\n" +
                "  } ]\n" +
                "}";

        HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(data)).build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
