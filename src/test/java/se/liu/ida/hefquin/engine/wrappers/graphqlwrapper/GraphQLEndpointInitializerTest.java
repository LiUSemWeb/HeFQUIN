package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl.GraphQLEndpointInitializerImpl;

/**
 * Unit tests for the GraphQLEndpoint initializer
 */
public class GraphQLEndpointInitializerTest extends EngineTestBase {
    
    final GraphQLEndpointInitializer initializer = new GraphQLEndpointInitializerImpl();

    @Test
    public void testGraphQLEndpointSetup() throws FederationAccessException{
        if(!skipLocalGraphQLTests){
            final GraphQLEndpoint endpoint = initializer.initializeEndpoint("http://localhost:4000/graphql",
                5000,5000);

            // Check that all expected GraphQL types have been initialized correctly
            assert(endpoint.containsGraphQLObjectType("Author"));
            assert(endpoint.containsGraphQLObjectType("Book"));

            // Check that all expected GraphQL type fields have been initialized correctly
            // - Author fields
            assert(endpoint.containsGraphQLField("Author","id"));
            assertEquals(GraphQLFieldType.SCALAR, endpoint.getGraphQLFieldType("Author", "id"));
            assertEquals("ID", endpoint.getGraphQLFieldValueType("Author", "id"));

            assert(endpoint.containsGraphQLField("Author","name"));
            assertEquals(GraphQLFieldType.SCALAR, endpoint.getGraphQLFieldType("Author", "name"));
            assertEquals("String", endpoint.getGraphQLFieldValueType("Author", "name"));

            assert(endpoint.containsGraphQLField("Author","age"));
            assertEquals(GraphQLFieldType.SCALAR, endpoint.getGraphQLFieldType("Author", "age"));
            assertEquals("Int", endpoint.getGraphQLFieldValueType("Author", "age"));

            assert(endpoint.containsGraphQLField("Author","books"));
            assertEquals(GraphQLFieldType.OBJECT, endpoint.getGraphQLFieldType("Author", "books"));
            assertEquals("Book", endpoint.getGraphQLFieldValueType("Author", "books"));

            // - Book fields
            assert(endpoint.containsGraphQLField("Book","id"));
            assertEquals(GraphQLFieldType.SCALAR, endpoint.getGraphQLFieldType("Book", "id"));
            assertEquals("ID", endpoint.getGraphQLFieldValueType("Book", "id"));

            assert(endpoint.containsGraphQLField("Book","title"));
            assertEquals(GraphQLFieldType.SCALAR, endpoint.getGraphQLFieldType("Book", "title"));
            assertEquals("String", endpoint.getGraphQLFieldValueType("Book", "title"));

            assert(endpoint.containsGraphQLField("Book","nr_pages"));
            assertEquals(GraphQLFieldType.SCALAR, endpoint.getGraphQLFieldType("Book", "nr_pages"));
            assertEquals("Int", endpoint.getGraphQLFieldValueType("Book", "nr_pages"));

            assert(endpoint.containsGraphQLField("Book","genre"));
            assertEquals(GraphQLFieldType.SCALAR, endpoint.getGraphQLFieldType("Book", "genre"));
            assertEquals("Genre", endpoint.getGraphQLFieldValueType("Book", "genre"));

            assert(endpoint.containsGraphQLField("Book","authors"));
            assertEquals(GraphQLFieldType.OBJECT, endpoint.getGraphQLFieldType("Book", "authors"));
            assertEquals("Author", endpoint.getGraphQLFieldValueType("Book", "authors"));


            // Check that all GraphQLEntrypoints have been setup correctly
            final GraphQLEntrypoint ep1 = endpoint.getEntrypoint("Author", GraphQLEntrypointType.SINGLE);
            final Map<String,String> ep1Args = ep1.getArgumentDefinitions();
            assertEquals("author", ep1.getFieldName());
            assertEquals("Author", ep1.getTypeName());
            assert(ep1Args.containsKey("id"));
            assertEquals("ID!", ep1Args.get("id"));

            final GraphQLEntrypoint ep2 = endpoint.getEntrypoint("Author", GraphQLEntrypointType.FILTERED);
            final Map<String,String> ep2Args = ep2.getArgumentDefinitions();
            assertEquals("authors", ep2.getFieldName());
            assertEquals("Author", ep2.getTypeName());
            assert(ep2Args.containsKey("name"));
            assertEquals("String", ep2Args.get("name"));
            assert(ep2Args.containsKey("age"));
            assertEquals("Int", ep2Args.get("age"));

            final GraphQLEntrypoint ep3 = endpoint.getEntrypoint("Author", GraphQLEntrypointType.FULL);
            final Map<String,String> ep3Args = ep3.getArgumentDefinitions();
            assertEquals("allAuthors", ep3.getFieldName());
            assertEquals("Author", ep3.getTypeName());
            assert(ep3Args.isEmpty());

            final GraphQLEntrypoint ep4 = endpoint.getEntrypoint("Book", GraphQLEntrypointType.SINGLE);
            final Map<String,String> ep4Args = ep4.getArgumentDefinitions();
            assertEquals("book", ep4.getFieldName());
            assertEquals("Book", ep4.getTypeName());
            assert(ep4Args.containsKey("id"));
            assertEquals("ID!", ep4Args.get("id"));

            final GraphQLEntrypoint ep5 = endpoint.getEntrypoint("Book", GraphQLEntrypointType.FILTERED);
            final Map<String,String> ep5Args = ep5.getArgumentDefinitions();
            assertEquals("books", ep5.getFieldName());
            assertEquals("Book", ep5.getTypeName());
            assert(ep5Args.containsKey("title"));
            assertEquals("String", ep5Args.get("title"));
            assert(ep5Args.containsKey("nr_pages"));
            assertEquals("Int", ep5Args.get("nr_pages"));
            assert(ep5Args.containsKey("genre"));
            assertEquals("Genre", ep5Args.get("genre"));

            final GraphQLEntrypoint ep6 = endpoint.getEntrypoint("Book", GraphQLEntrypointType.FULL);
            final Map<String,String> ep6Args = ep6.getArgumentDefinitions();
            assertEquals("allBooks", ep6.getFieldName());
            assertEquals("Book", ep6.getTypeName());
            assert(ep6Args.isEmpty());
        }
    }
}
