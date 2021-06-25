package se.liu.ida.hefquin.engine.federation.access;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Neo4jConnection {
    private final HttpPost request;

    public Neo4jConnection(HttpPost request) {
        this.request = request;
    }

    public String executeQuery(String cypher) throws IOException {
        String entity = "{\n" +
                "  \"statements\" : [ {\n" +
                "    \"statement\" : \""+cypher+"\",\n" +
                "    \"parameters\" : {}\n" +
                "  } ]\n" +
                "}";
        request.setEntity(new StringEntity(entity));
        String result = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            result = EntityUtils.toString(response.getEntity());
        }

        return result;
    }
}
