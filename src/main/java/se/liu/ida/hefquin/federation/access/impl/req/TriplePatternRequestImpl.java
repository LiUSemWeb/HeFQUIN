package se.liu.ida.hefquin.federation.access.impl.req;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.query.TriplePattern;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedQueryPatternUtils;

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
		return JenaBasedQueryPatternUtils.getVariablesInPattern(tp);
	}

}
