package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.GraphQLRequest;
import se.liu.ida.hefquin.engine.federation.access.JSONResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.GraphQLRequestImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl.GraphQLQueryImpl;

public class GraphQLRequestProcessorImplTest extends EngineTestBase {

    @Test
    public void testLocalEndpoint() throws FederationAccessException {
        if (!skipLocalGraphQLTests) {
            TreeSet<String> fieldPaths = new TreeSet<>();
            Map<String, String> parameterDefinitions = new HashMap<>();
            JsonObject parameterValues = new JsonObject();

            fieldPaths.add("author(id:$id)/name");
            fieldPaths.add("author(id:$id)/books/title");
            fieldPaths.add("author(id:$id)/books/nr_pages");
            fieldPaths.add("books/title");
            fieldPaths.add("books/authors/name");

            parameterDefinitions.put("id", "ID!");
            parameterValues.put("id", "auth3");

            final GraphQLQuery query = new GraphQLQueryImpl(fieldPaths, parameterValues, parameterDefinitions);
            final GraphQLRequest req = new GraphQLRequestImpl(query);
            final GraphQLEndpoint fm = new GraphQLEndpointTest();
            final GraphQLRequestProcessor processor = new GraphQLRequestProcessorImpl(3000, 3000);
            final JSONResponse response = processor.performRequest(req, fm);

            assertEquals(fm, response.getFederationMember());
            assertEquals(req, response.getRequest());
        }
    }
}
