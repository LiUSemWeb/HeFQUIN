package se.liu.ida.hefquin.engine.wrappers.graphql.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.wrappers.graphql.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.impl.GraphQLEntrypointType;

/**
 * An class for objects that wrap a {@link StarPattern} and provide
 * functionality for creating the root field of a GraphQL query
 * based on the star pattern.
 */
public class GraphQLQueryRootForStarPattern
{
	protected final StarPattern sp;
	protected final GraphQL2RDFConfiguration config;
	protected final GraphQLSchema schema;

	private boolean graphqlTypeHasBeenDetermined = false;
	private String graphqlType = null;
	private Map<String, LiteralLabel> graphqlArguments = null;
	private GraphQLEntrypoint graphqlEntryPoint = null;

	public GraphQLQueryRootForStarPattern( final StarPattern sp,
	                                       final GraphQL2RDFConfiguration config,
	                                       final GraphQLSchema schema ) {
		this.sp         = sp;
		this.config     = config;
		this.schema     = schema;
	}

	public StarPattern getStarPattern() { return sp; }

	/**
	 * Returns the GraphQL object type that corresponds to the star
	 * pattern or <code>null</code> if the type cannot be determined.
	 */
	public final String getGraphQLObjectType() {
		if ( ! graphqlTypeHasBeenDetermined ) {
			graphqlType = determineGraphQLObjectType();
			graphqlTypeHasBeenDetermined = true;
		}

		return graphqlType;
	}

	/**
	 * Returns a map representing the arguments that can be used from the given star pattern.
	 * The predicate from a TP needs to be a property URI and the object needs to be a literal
	 */
	public final Map<String, LiteralLabel> getGraphQLArguments() {
		if ( graphqlArguments == null ) {
			graphqlArguments = determineGraphQLArguments();
		}

		return graphqlArguments;
	}

	/**
	 * Returns the relevant entry point with regards to if the corresponding
	 * star pattern has the required arguments.
	 * @throws QueryTranslatingException if no valid entrypoint was found.
	 */
	public final GraphQLEntrypoint getGraphQLEntryPoint() throws QueryTranslatingException {
		if ( graphqlEntryPoint == null ) {
			graphqlEntryPoint = determineGraphQLEntryPoint();
		}

		return graphqlEntryPoint;
		
	}


	protected String determineGraphQLObjectType() {
		for ( final TriplePattern tp : sp.getTriplePatterns() ) {
			final Node predicate = tp.asJenaTriple().getPredicate();
			final Node object    = tp.asJenaTriple().getObject();

			if ( predicate.isURI() ) {
				final String predURI = predicate.getURI();
				if ( config.isValidPropertyURI(predURI) ) {
					return config.mapPropertyToType(predURI);
				}
				else if ( config.isValidMembershipURI(predURI) && object.isURI() ) {
					final String objURI = object.getURI();
					if ( config.isValidClassURI(objURI) ) {
						return config.mapClassToType(objURI);
					}
				}
			}
		}

		return null;
	}

	protected Map<String, LiteralLabel> determineGraphQLArguments() {
		final Map<String, LiteralLabel> args = new HashMap<>();
		for ( final TriplePattern tp : sp.getTriplePatterns() ) {
			final Node predicate = tp.asJenaTriple().getPredicate();
			final Node object    = tp.asJenaTriple().getObject();

			if ( predicate.isURI() && config.isValidPropertyURI(predicate.getURI()) ) {
				if ( object.isLiteral() ) {
					args.put( config.mapPropertyToField(predicate.toString()), object.getLiteral() );
				}
			}
		}

		return args;
	}

	protected GraphQLEntrypoint determineGraphQLEntryPoint() throws QueryTranslatingException {
		final String type = getGraphQLObjectType();
		if ( type == null ) {
			throw new IllegalStateException();
		}

		final Set<String> argNames = getGraphQLArguments().keySet();

		// First, try single-object entry point
		final GraphQLEntrypoint e1 = schema.getEntrypoint(type, GraphQLEntrypointType.SINGLE);
		if (e1 != null && SPARQL2GraphQLHelper.hasAllNecessaryArguments(argNames, e1.getArgumentDefinitions().keySet()) ) {
			return e1;
		}

		// Next, try filtered list entry point
		final GraphQLEntrypoint e2 = schema.getEntrypoint(type, GraphQLEntrypointType.FILTERED);
		if (e2 != null && SPARQL2GraphQLHelper.hasNecessaryArguments(argNames, e2.getArgumentDefinitions().keySet()) ) {
			return e2;
		}

		// Get full list entry point (No argument values)
		final GraphQLEntrypoint e3 = schema.getEntrypoint(type, GraphQLEntrypointType.FULL);

		if(e3 == null){
			throw new QueryTranslatingException("No valid entrypoint for the star pattern was found!");
		}
		
		return e3;
	}

	@Override
	public boolean equals(final Object o) {
		if(this == o){
			return true;
		}

		if(!(o instanceof GraphQLQueryRootForStarPattern)){
			return false;
		}

		final GraphQLQueryRootForStarPattern that = (GraphQLQueryRootForStarPattern) o;
		return this.sp.getSubject().equals(that.sp.getSubject());
	}

	@Override
	public int hashCode() {
		return sp.getSubject().hashCode();
	}
	
}
