package se.liu.ida.hefquin.engine.query.jenaimpl;

import java.util.Collections;
import java.util.Set;

import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class JenaBasedBGP implements BGP
{
	private final Set<TriplePattern> tps;

	public JenaBasedBGP( final Set<TriplePattern> tps ) {
		assert tps != null;
		this.tps = tps;
	}

	@Override
	public Set<TriplePattern> getTriplePatterns() {
		return Collections.unmodifiableSet(tps);
	}

}
