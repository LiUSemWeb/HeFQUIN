package se.liu.ida.hefquin.federation.access.impl.req;

import java.util.Set;

import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.query.SolutionMapping;
import se.liu.ida.hefquin.query.TriplePattern;

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

}
