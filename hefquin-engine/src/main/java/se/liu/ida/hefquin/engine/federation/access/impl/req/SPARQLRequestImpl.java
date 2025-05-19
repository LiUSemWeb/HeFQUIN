package se.liu.ida.hefquin.engine.federation.access.impl.req;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;

public class SPARQLRequestImpl implements SPARQLRequest
{
	protected final SPARQLGraphPattern pattern;
	protected final SPARQLQuery query;
	protected final ExpectedVariables expectedVars;

	public SPARQLRequestImpl( final SPARQLGraphPattern pattern ) {
		assert pattern != null;
		this.pattern = pattern;
		this.query = null;

		// we materialize the ExpectedVariables in this case
		// to avoid producing it again whenever it is used
		expectedVars = pattern.getExpectedVariables();
	}

	public SPARQLRequestImpl( final SPARQLQuery query ) {
		assert query != null;
		this.query = query;
		this.pattern = null;

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
			return oo.getQueryPattern() == null && query.equals( oo.getQuery() );
		}
		else {
			return pattern.equals( oo.getQueryPattern() );
		}
	}

	@Override
	public int hashCode(){
		if ( pattern == null )
			return query.hashCode();
		else
			return pattern.hashCode();
	}

	@Override
	public SPARQLGraphPattern getQueryPattern() {
		return pattern;
	}

	@Override
	public SPARQLQuery getQuery() {
		if ( query != null )
			return query;
		else
			return SPARQLRequest.convertToQuery( getQueryPattern() );
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return expectedVars;
	}

	@Override
	public String toString(){
		if ( query != null ) {
			return query.toString();
		}
		else if ( pattern != null ){
			return pattern.toString();
		}
		else
			return "The query pattern of SPARQLRequest is null";
	}

}
