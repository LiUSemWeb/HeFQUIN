package se.liu.ida.hefquin.base.query;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public interface SPARQLQuery extends Query
{
	/**
	 * Returns a representation of this SPARQL query as an object of the
	 * {@link org.apache.jena.query.Query} class of the Jena API.
	 */
	org.apache.jena.query.Query asJenaQuery();

	/**
	 * Returns the sets of variables that can be expected in the
	 * solution mappings produced for this query.
	 */
	default ExpectedVariables getExpectedVariables() {
		final Set<Var> vars = new HashSet<>( asJenaQuery().getProjectVars() );

		return new ExpectedVariables() {
			@Override public Set<Var> getPossibleVariables() { return vars; }
			@Override public Set<Var> getCertainVariables() { return Collections.emptySet(); }
		};
	}

}
