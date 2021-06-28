package se.liu.ida.hefquin.engine.federation.access;

import java.net.URI;
import java.net.http.HttpRequest;

public class Neo4jConnectionFactory {
    static public Neo4jConnection connect(final String URL){
        HttpRequest.Builder request = HttpRequest.newBuilder(
                URI.create(URL))
                .header("Accept", "application/json;charset=UTF-8")
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic c2ZlcnJhZGE6YWRtaW4=");
        return new Neo4jConnection(request);
    }
}
