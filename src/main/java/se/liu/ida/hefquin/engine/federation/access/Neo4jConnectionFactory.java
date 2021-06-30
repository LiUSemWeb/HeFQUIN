package se.liu.ida.hefquin.engine.federation.access;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Neo4jConnectionFactory {

    static public Neo4jConnection connect(final String URL){
        return new Neo4jConnection(URL);
    }

    public static class Neo4jConnection {
        private String URL;

        public Neo4jConnection( final String URL ) {
            assert URL != null;
            this.URL = URL;
        }

        public String executeQuery(String cypher) {
            String data = "{\n" +
                    "  \"statements\" : [ {\n" +
                    "    \"statement\" : \""+cypher+"\",\n" +
                    "    \"parameters\" : {}\n" +
                    "  } ]\n" +
                    "}";

            var request = HttpRequest.newBuilder(
                    URI.create(this.URL))
                    .header("Accept", "application/json;charset=UTF-8")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(data))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            } catch (IOException e) {
                throw new Neo4JConnectionException("Data could not be sent to the server");
            } catch (InterruptedException e) {
                throw new Neo4JConnectionException("Neo4j server could not be reached.");
            }
        }
    }
}
