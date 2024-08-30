package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;
import org.junit.Test;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQLException;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQLSchemaInitializer;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

public class GraphQLSchemaInitializerImplTest
{
	/**
	 * If true, skip tests to local GraphQL endpoint
	 */
	public static boolean skipLocalGraphQLTests = true;
    
    final GraphQLSchemaInitializer initializer = new GraphQLSchemaInitializerImpl();

    @Test
    public void testGraphQLEndpointSetup() throws GraphQLException, ParseException {
        if(!skipLocalGraphQLTests){
            final GraphQLSchema schema = initializer.initializeSchema("http://localhost:4000/graphql",
                5000,5000);

            // Check that all expected GraphQL types have been initialized correctly
            assert(schema.containsGraphQLObjectType("Author"));
            assert(schema.containsGraphQLObjectType("Book"));

            // Check that all expected GraphQL type fields have been initialized correctly
            // - Author fields
            assert(schema.containsGraphQLField("Author","id"));
            assertEquals(GraphQLFieldType.SCALAR, schema.getGraphQLFieldType("Author", "id"));
            assertEquals("ID", schema.getGraphQLFieldValueType("Author", "id"));

            assert(schema.containsGraphQLField("Author","name"));
            assertEquals(GraphQLFieldType.SCALAR, schema.getGraphQLFieldType("Author", "name"));
            assertEquals("String", schema.getGraphQLFieldValueType("Author", "name"));

            assert(schema.containsGraphQLField("Author","age"));
            assertEquals(GraphQLFieldType.SCALAR, schema.getGraphQLFieldType("Author", "age"));
            assertEquals("Int", schema.getGraphQLFieldValueType("Author", "age"));

            assert(schema.containsGraphQLField("Author","books"));
            assertEquals(GraphQLFieldType.OBJECT, schema.getGraphQLFieldType("Author", "books"));
            assertEquals("Book", schema.getGraphQLFieldValueType("Author", "books"));

            // - Book fields
            assert(schema.containsGraphQLField("Book","id"));
            assertEquals(GraphQLFieldType.SCALAR, schema.getGraphQLFieldType("Book", "id"));
            assertEquals("ID", schema.getGraphQLFieldValueType("Book", "id"));

            assert(schema.containsGraphQLField("Book","title"));
            assertEquals(GraphQLFieldType.SCALAR, schema.getGraphQLFieldType("Book", "title"));
            assertEquals("String", schema.getGraphQLFieldValueType("Book", "title"));

            assert(schema.containsGraphQLField("Book","nr_pages"));
            assertEquals(GraphQLFieldType.SCALAR, schema.getGraphQLFieldType("Book", "nr_pages"));
            assertEquals("Int", schema.getGraphQLFieldValueType("Book", "nr_pages"));

            assert(schema.containsGraphQLField("Book","genre"));
            assertEquals(GraphQLFieldType.SCALAR, schema.getGraphQLFieldType("Book", "genre"));
            assertEquals("Genre", schema.getGraphQLFieldValueType("Book", "genre"));

            assert(schema.containsGraphQLField("Book","authors"));
            assertEquals(GraphQLFieldType.OBJECT, schema.getGraphQLFieldType("Book", "authors"));
            assertEquals("Author", schema.getGraphQLFieldValueType("Book", "authors"));


            // Check that all GraphQLEntrypoints have been setup correctly
            final GraphQLEntrypoint ep1 = schema.getEntrypoint("Author", GraphQLEntrypointType.SINGLE);
            final Map<String,String> ep1Args = ep1.getArgumentDefinitions();
            assertEquals("author", ep1.getFieldName());
            assertEquals("Author", ep1.getTypeName());
            assert(ep1Args.containsKey("id"));
            assertEquals("ID!", ep1Args.get("id"));

            final GraphQLEntrypoint ep2 = schema.getEntrypoint("Author", GraphQLEntrypointType.FILTERED);
            final Map<String,String> ep2Args = ep2.getArgumentDefinitions();
            assertEquals("authors", ep2.getFieldName());
            assertEquals("Author", ep2.getTypeName());
            assert(ep2Args.containsKey("name"));
            assertEquals("String", ep2Args.get("name"));
            assert(ep2Args.containsKey("age"));
            assertEquals("Int", ep2Args.get("age"));

            final GraphQLEntrypoint ep3 = schema.getEntrypoint("Author", GraphQLEntrypointType.FULL);
            final Map<String,String> ep3Args = ep3.getArgumentDefinitions();
            assertEquals("allAuthors", ep3.getFieldName());
            assertEquals("Author", ep3.getTypeName());
            assert(ep3Args.isEmpty());

            final GraphQLEntrypoint ep4 = schema.getEntrypoint("Book", GraphQLEntrypointType.SINGLE);
            final Map<String,String> ep4Args = ep4.getArgumentDefinitions();
            assertEquals("book", ep4.getFieldName());
            assertEquals("Book", ep4.getTypeName());
            assert(ep4Args.containsKey("id"));
            assertEquals("ID!", ep4Args.get("id"));

            final GraphQLEntrypoint ep5 = schema.getEntrypoint("Book", GraphQLEntrypointType.FILTERED);
            final Map<String,String> ep5Args = ep5.getArgumentDefinitions();
            assertEquals("books", ep5.getFieldName());
            assertEquals("Book", ep5.getTypeName());
            assert(ep5Args.containsKey("title"));
            assertEquals("String", ep5Args.get("title"));
            assert(ep5Args.containsKey("nr_pages"));
            assertEquals("Int", ep5Args.get("nr_pages"));
            assert(ep5Args.containsKey("genre"));
            assertEquals("Genre", ep5Args.get("genre"));

            final GraphQLEntrypoint ep6 = schema.getEntrypoint("Book", GraphQLEntrypointType.FULL);
            final Map<String,String> ep6Args = ep6.getArgumentDefinitions();
            assertEquals("allBooks", ep6.getFieldName());
            assertEquals("Book", ep6.getTypeName());
            assert(ep6Args.isEmpty());
        }
    }
}
