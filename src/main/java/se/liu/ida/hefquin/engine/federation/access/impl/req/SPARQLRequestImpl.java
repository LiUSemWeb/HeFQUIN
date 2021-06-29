package se.liu.ida.hefquin.engine.federation.access.impl.req;

import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

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
		expectedVars = QueryPatternUtils.getExpectedVariablesInPattern(pattern);
	}

	public SPARQLRequestImpl( final SPARQLQuery query ) {
		assert query != null;
		this.query = query;
		this.pattern = null;

		// we materialize the ExpectedVariables in this case
		// to avoid producing it again whenever it is used
		expectedVars = QueryPatternUtils.getExpectedVariablesInQuery(query);
	}

	public SPARQLGraphPattern getQueryPattern() {
		return pattern;
	}

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

}
