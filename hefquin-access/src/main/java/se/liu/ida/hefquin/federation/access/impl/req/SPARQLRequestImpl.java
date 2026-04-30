package se.liu.ida.hefquin.federation.access.impl.req;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
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
		if ( query != null )
			return query;
		else
			return SPARQLRequest.convertToQuery( getQueryPattern(), projectionVars, isDistinct );
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

}
