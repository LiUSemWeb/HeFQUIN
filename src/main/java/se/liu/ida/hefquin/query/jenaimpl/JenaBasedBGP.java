package se.liu.ida.hefquin.query.jenaimpl;

import java.util.Collections;
import java.util.Set;

import se.liu.ida.hefquin.query.BGP;

public class JenaBasedBGP implements BGP
{
	private final Set<JenaBasedTriplePattern> tps;

	public JenaBasedBGP( final Set<JenaBasedTriplePattern> tps ) {
		assert tps != null;
		this.tps = tps;
	}

	@Override
	public Set<JenaBasedTriplePattern> getTriplePatterns() {
		return Collections.unmodifiableSet(tps);
	}

}
