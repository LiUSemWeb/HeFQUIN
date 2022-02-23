package se.liu.ida.hefquin.engine.query.impl;

import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;

public class SPARQLUnionPatternImpl implements SPARQLUnionPattern
{
	protected final List<SPARQLGraphPattern> subPatterns;

	public SPARQLUnionPatternImpl( final List<SPARQLGraphPattern> subPatterns ) {
		assert subPatterns.size() > 1;
		this.subPatterns = subPatterns;
	}

	public SPARQLUnionPatternImpl( final SPARQLGraphPattern ... subPatterns ) {
		assert subPatterns.length > 1;
		this.subPatterns = Arrays.asList(subPatterns);
	}

	@Override
	public int getNumberOfSubPatterns() {
		return subPatterns.size();
	}

	@Override
	public Iterable<SPARQLGraphPattern> getSubPatterns() {
		return subPatterns;
	}

	@Override
	public SPARQLGraphPattern getSubPatterns( final int i ) throws IndexOutOfBoundsException {
		return subPatterns.get(i);
	}

}
