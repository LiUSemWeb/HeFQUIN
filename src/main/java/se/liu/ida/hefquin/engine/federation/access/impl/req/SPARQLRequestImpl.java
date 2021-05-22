package se.liu.ida.hefquin.engine.federation.access.impl.req;

import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

public class SPARQLRequestImpl implements SPARQLRequest
{
	protected final SPARQLGraphPattern pattern;
	protected final ExpectedVariables expectedVars;

	public SPARQLRequestImpl( final SPARQLGraphPattern pattern ) {
		assert pattern != null;
		this.pattern = pattern;

		// we materialize the ExpectedVariables in this case
		// to avoid producing it again whenever it is used
		expectedVars = QueryPatternUtils.getExpectedVariablesInPattern(pattern);
	}

	public SPARQLGraphPattern getQueryPattern() {
		return pattern;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return expectedVars;
	}

}
