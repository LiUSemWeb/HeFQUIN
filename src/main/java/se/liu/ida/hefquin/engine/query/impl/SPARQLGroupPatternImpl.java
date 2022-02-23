package se.liu.ida.hefquin.engine.query.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLGroupPattern;

public class SPARQLGroupPatternImpl implements SPARQLGroupPattern
{
	protected final List<SPARQLGraphPattern> subPatterns;

	public SPARQLGroupPatternImpl( final List<SPARQLGraphPattern> subPatterns ) {
		assert subPatterns.size() > 1;
		this.subPatterns = subPatterns;
	}

	public SPARQLGroupPatternImpl( final SPARQLGraphPattern ... subPatterns ) {
		assert subPatterns.length > 1;
		this.subPatterns = Arrays.asList(subPatterns);
	}

	@Override
	public Element asJenaElement() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Op asJenaOp() {
		throw new UnsupportedOperationException();
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
