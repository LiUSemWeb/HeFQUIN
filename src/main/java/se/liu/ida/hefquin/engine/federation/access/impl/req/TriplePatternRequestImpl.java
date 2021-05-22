package se.liu.ida.hefquin.engine.federation.access.impl.req;

import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

public class TriplePatternRequestImpl implements TriplePatternRequest
{
	protected final TriplePattern tp;

	public TriplePatternRequestImpl( final TriplePattern tp ) {
		assert tp != null;
		this.tp = tp;
	}

	public TriplePattern getQueryPattern() {
		return tp;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return QueryPatternUtils.getExpectedVariablesInPattern(tp);
	}

}
