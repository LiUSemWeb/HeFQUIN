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
	 * Returns the set of variables projected by this query.
	 *
	 * <p>This corresponds to the variables in the SELECT clause of the query.
	 * For {@code SELECT *} queries, this typically reflects the variables
	 * inferred by the underlying query representation.</p>
	 *
	 * @return the set of projected variables (never {@code null})
	 */
	Set<Var> getProjectionVars();

	/**
	 * Indicates whether this query enforces duplicate elimination.
	 *
	 * <p>This corresponds to the presence of the {@code DISTINCT} modifier
	 * in the SELECT clause.</p>
	 *
	 * @return {@code true} if the query is DISTINCT; {@code false} otherwise
	 */
	boolean isDistinct();

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
