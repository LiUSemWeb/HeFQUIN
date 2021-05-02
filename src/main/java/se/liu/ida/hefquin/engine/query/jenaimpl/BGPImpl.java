package se.liu.ida.hefquin.engine.query.jenaimpl;

import java.util.Collections;
import java.util.Set;

import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class BGPImpl implements BGP
{
	private final Set<TriplePattern> tps;

	public BGPImpl( final Set<TriplePattern> tps ) {
		assert tps != null;
		this.tps = tps;
	}

	@Override
	public Element asJenaElement() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<TriplePattern> getTriplePatterns() {
		return Collections.unmodifiableSet(tps);
	}

}
