package se.liu.ida.hefquin.engine.federation.access.impl.req;

import java.util.Set;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

public class BindingsRestrictedTriplePatternRequestImpl implements BindingsRestrictedTriplePatternRequest
{
	protected final TriplePattern tp;
	protected final Set<SolutionMapping> solMaps;

	public BindingsRestrictedTriplePatternRequestImpl(
			final TriplePattern tp,
			final Set<SolutionMapping> solMaps ) {
		assert tp != null;
		assert solMaps != null;

		this.tp = tp;
		this.solMaps = solMaps;
	}

	public TriplePattern getTriplePattern() {
		return tp;
	}

	public Set<SolutionMapping> getSolutionMappings() {
		return solMaps;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return QueryPatternUtils.getExpectedVariablesInPattern(tp);
	}

}
