package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;

/**
 * An class for objects that wrap a {@link StarPattern} and provide
 * functionality for creating the root field of a GraphQL query
 * based on the star pattern.
 */
public class GraphQLQueryRootForStarPattern
{
	protected final StarPattern sp;
	protected final GraphQL2RDFConfiguration config;
	protected final GraphQLEndpoint endpoint;

	private boolean graphqlTypeHasBeenDetermined = false;
	private String graphqlType = null;
	private Map<String, LiteralLabel> graphqlArguments = null;
	private GraphQLEntrypoint graphqlEntryPoint = null;

	public GraphQLQueryRootForStarPattern( final StarPattern sp,
	                                       final GraphQL2RDFConfiguration config,
	                                       final GraphQLEndpoint endpoint ) {
		this.sp         = sp;
		this.config     = config;
		this.endpoint   = endpoint;
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
	 */
	public final GraphQLEntrypoint getGraphQLEntryPoint() {
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

	protected GraphQLEntrypoint determineGraphQLEntryPoint() {
		final String type = getGraphQLObjectType();
		if ( type == null ) {
			throw new IllegalStateException();
		}

		final Set<String> argNames = getGraphQLArguments().keySet();

		// First, try single-object entry point
		final GraphQLEntrypoint e1 = endpoint.getEntrypoint(type, GraphQLEntrypointType.SINGLE);
		if ( SPARQL2GraphQLHelper.hasAllNecessaryArguments(argNames, e1.getArgumentDefinitions().keySet()) ) {
			return e1;
			}

		// Next, try filtered list entry point
		final GraphQLEntrypoint e2 = endpoint.getEntrypoint(type, GraphQLEntrypointType.FILTERED);
		if ( SPARQL2GraphQLHelper.hasNecessaryArguments(argNames, e2.getArgumentDefinitions().keySet()) ) {
			return e2;
		}

		// Get full list entry point (No argument values)
		return endpoint.getEntrypoint(type, GraphQLEntrypointType.FULL);
	}

}
