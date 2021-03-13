package se.liu.ida.hefquin.federation.access.impl.req;

import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.query.TriplePattern;

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

}
