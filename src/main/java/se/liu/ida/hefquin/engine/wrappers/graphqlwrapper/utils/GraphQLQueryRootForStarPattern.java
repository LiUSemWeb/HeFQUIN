package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;

/**
 * An class for objects that wrap a {@link StarPattern} and provide
 * functionality for creating the root field of a GraphQL query
 * based on the star pattern.
 */
public class GraphQLQueryRootForStarPattern
{
	protected final GraphQL2RDFConfiguration config;
	protected final StarPattern sp;

	private boolean graphqlTypeHasBeenDetermined = false;
	private String graphqlType = null;

	public GraphQLQueryRootForStarPattern( final GraphQL2RDFConfiguration config,
	                                       final StarPattern sp ) {
		this.config = config;
		this.sp = sp;
	}

	public StarPattern getStarPattern() { return sp; }

	/**
	 * Returns the GraphQL object type that corresponds to the star
	 * pattern or <code>null</code> if the type cannot be determined.
	 */
	public String getGraphQLObjectType() {
		if ( ! graphqlTypeHasBeenDetermined ) {
			graphqlType = determineGraphQLObjectType();
			graphqlTypeHasBeenDetermined = true;
		}

		return graphqlType;
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

}
