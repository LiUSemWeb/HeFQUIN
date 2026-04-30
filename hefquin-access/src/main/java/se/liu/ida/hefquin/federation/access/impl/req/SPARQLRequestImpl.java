package se.liu.ida.hefquin.federation.access.impl.req;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
import se.liu.ida.hefquin.base.query.impl.SPARQLQueryImpl;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;

public class SPARQLRequestImpl implements SPARQLRequest
{
	protected final SPARQLGraphPattern pattern;
	protected final SPARQLQuery query;
	protected final ExpectedVariables expectedVars;
	protected final Set<Var> projectionVars;
	protected final boolean isDistinct;

	public SPARQLRequestImpl( final SPARQLGraphPattern pattern ) {
		assert pattern != null;
		this.pattern = pattern;
		this.query = null;
		this.projectionVars = null;
		this.isDistinct = false;

		// we materialize the ExpectedVariables in this case
		// to avoid producing it again whenever it is used
		expectedVars = pattern.getExpectedVariables();
	}

	public SPARQLRequestImpl( final SPARQLGraphPattern pattern,
	                          final Set<Var> projectionVars,
	                          final boolean distinct ) {
		assert pattern != null;
		this.pattern = pattern;
		this.query = null;
		this.projectionVars = projectionVars;
		this.isDistinct = distinct;

		// we materialize the ExpectedVariables in this case
		// to avoid producing it again whenever it is used
		expectedVars = pattern.getExpectedVariables();
	}

	public SPARQLRequestImpl( final SPARQLQuery query ) {
		assert query != null;
		this.query = query;
		this.pattern = null;
		this.projectionVars = new HashSet<>( query.asJenaQuery().getProjectVars() );
		this.isDistinct = query.asJenaQuery().isDistinct();

		// we materialize the ExpectedVariables in this case
		// to avoid producing it again whenever it is used
		expectedVars = query.getExpectedVariables();
	}

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof SPARQLRequest) )
			return false;

		final SPARQLRequest oo = (SPARQLRequest) o;

		if ( pattern == null ) {
			return oo.getQueryPattern() == null
			    && query.equals( oo.getQuery() )
			    && getProjectionVars().equals(oo.getProjectionVars())
			    && isDistinct() == oo.isDistinct();
		}
		else {
			return pattern.equals( oo.getQueryPattern() )
			    && getProjectionVars().equals(oo.getProjectionVars())
			    && isDistinct() == oo.isDistinct();
		}
	}

	@Override
	public int hashCode() {
		if ( pattern == null )
			return query.hashCode() ^ getProjectionVars().hashCode() ^ (isDistinct() ? 1 : 0);
		else
			return pattern.hashCode() ^ getProjectionVars().hashCode() ^ (isDistinct() ? 1 : 0);
	}

	@Override
	public SPARQLGraphPattern getQueryPattern() {
		return pattern;
	}

	@Override
	public Set<Var> getProjectionVars() {
		return projectionVars;
	}

	@Override
	public boolean isDistinct() {
		return isDistinct;
	}

	@Override
	public SPARQLQuery getQuery() {
		final SPARQLQuery baseQuery = query != null ? query : SPARQLRequest.convertToQuery( getQueryPattern() );

		if ( getProjectionVars() == null && ! isDistinct() )
			return baseQuery;

		// Clone the query to avoid mutating the original request
		final Query q = baseQuery.asJenaQuery().cloneQuery();

		// Apply request-level projection if specified and safe.
		// This replaces the SELECT clause with the given variables.
		// Note: This is only done when it does not interfere with query semantics
		// (e.g., no aggregation or grouping present).
		if ( getProjectionVars() != null && isSafeToOverrideProjection(q) ) {
			q.setQueryResultStar(false);
			q.getProject().clear();
			getProjectionVars().forEach(q::addResultVar);
		}

		// Apply DISTINCT if requested.
		// This enforces duplicate elimination at the endpoint level.
		if ( isDistinct() )
			q.setDistinct( true );

		return new SPARQLQueryImpl(q);
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return expectedVars;
	}

	@Override
	public String toString(){
		if ( query != null )
			return "SPARQLRequest with query: " + query.toString();
		else
			return "SPARQLRequest with pattern: " + pattern.toString();
	}

	/**
	 * Determines whether it is semantically safe to override the projection
	 * of the given query.
	 *
	 * <p>Overriding the projection means replacing the SELECT clause with a
	 * new set of variables (e.g., for projection pushdown). This is only safe
	 * for "simple" SELECT queries without features that depend on the original
	 * projection.</p>
	 *
	 * <p>In particular, projection must not be overridden if the query contains:
	 * <ul>
	 *   <li>GROUP BY (projection affects grouping semantics)</li>
	 *   <li>HAVING clauses (depends on grouped results)</li>
	 *   <li>Aggregators (e.g., COUNT, SUM), which rely on specific projection expressions</li>
	 * </ul>
	 * </p>
	 *
	 * @param q the query to inspect
	 * @return {@code true} if projection can be safely overridden; {@code false} otherwise
	 */
	protected static boolean isSafeToOverrideProjection(final Query q)
	{
		// Query must be SELECT type
		if ( ! q.isSelectType() ) return false;

		// Must not contain GROUP BY
		if ( q.hasGroupBy() ) return false;

		// Must not contain HAVING (optional but safe)
		if ( q.hasHaving() ) return false;

		// Must not have expressions in SELECT
		if ( q.hasAggregators() ) return false;

		return true;
	}
}
