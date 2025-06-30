package se.liu.ida.hefquin.federation.access.impl.reqproc;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.json.JsonString;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLArgument;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLField;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.impl.GraphQLArgumentImpl;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphql.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphql.query.impl.GraphQLQueryImpl;
import se.liu.ida.hefquin.federation.FederationTestBase;
import se.liu.ida.hefquin.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.GraphQLInterface;
import se.liu.ida.hefquin.federation.access.GraphQLRequest;
import se.liu.ida.hefquin.federation.access.JSONResponse;
import se.liu.ida.hefquin.federation.access.impl.iface.GraphQLInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.req.GraphQLRequestImpl;

public class GraphQLRequestProcessorImplTest extends FederationTestBase {

    protected static class GraphQLEndpointTest implements GraphQLEndpoint {

		protected final GraphQLInterface iface = new GraphQLInterfaceImpl("http://localhost:4000/graphql");
		protected final GraphQLSchema schema = new GraphQLSchemaForTest();

		@Override
		public VocabularyMapping getVocabularyMapping() {
			throw new UnsupportedOperationException();
		}

		@Override
		public GraphQLInterface getInterface() {
			return iface;
		}

		@Override
		public GraphQLSchema getSchema() {
			return schema;
		}

	}

	protected static class GraphQLSchemaForTest implements GraphQLSchema
	{
		@Override
		public boolean containsGraphQLObjectType(String className) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean containsGraphQLField(String className, String propertyName) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public GraphQLFieldType getGraphQLFieldType(String className, String propertyName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getGraphQLFieldValueType(String className, String propertyName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<String> getGraphQLObjectTypes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, GraphQLField> getGraphQLObjectFields(String className) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GraphQLEntrypoint getEntrypoint(String className, GraphQLEntrypointType type) {
			// TODO Auto-generated method stub
			return null;
		}

	}

    @Test
    public void testLocalEndpoint() throws FederationAccessException {
        if (!skipLocalGraphQLTests) {
            final Set<String> fieldPaths = new HashSet<>();
			final Set<GraphQLArgument> queryArgs = new HashSet<>();

            fieldPaths.add("author(id:$id)/name");
            fieldPaths.add("author(id:$id)/books/title");
            fieldPaths.add("author(id:$id)/books/nr_pages");
            fieldPaths.add("books/title");
            fieldPaths.add("books/authors/name");

			queryArgs.add(new GraphQLArgumentImpl("id", "id", new JsonString("auth3"), "ID!"));

            final GraphQLQuery query = new GraphQLQueryImpl(fieldPaths, queryArgs);
            final GraphQLRequest req = new GraphQLRequestImpl(query);
            final GraphQLEndpoint fm = new GraphQLEndpointTest();
            final GraphQLRequestProcessor processor = new GraphQLRequestProcessorImpl(3000, 3000);
            final JSONResponse response = processor.performRequest(req, fm);
			
            assertEquals(fm, response.getFederationMember());
            assertEquals(req, response.getRequest());
        }
    }
}
