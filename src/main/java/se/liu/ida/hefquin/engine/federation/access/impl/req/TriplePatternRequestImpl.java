package se.liu.ida.hefquin.engine.federation.access.impl.req;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.jenaimpl.QueryPatternUtils;

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
	public Set<Var> getExpectedVariables() {
		return QueryPatternUtils.getVariablesInPattern(tp);
	}

}
