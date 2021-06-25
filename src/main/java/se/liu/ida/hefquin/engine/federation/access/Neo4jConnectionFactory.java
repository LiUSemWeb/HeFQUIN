package se.liu.ida.hefquin.engine.federation.access;

import org.apache.http.client.methods.HttpPost;

import java.net.URI;

public class Neo4jConnectionFactory {
    static public Neo4jConnection connect(final String URL){
        HttpPost request = new HttpPost(URL);
        request.addHeader("Accept", "application/json;charset=UTF-8");
        request.addHeader("Content-Type", "application/json");
        return new Neo4jConnection(request);
    }
}
