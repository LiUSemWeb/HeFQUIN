package se.liu.ida.hefquin.engine.query.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLGroupPattern;

public class SPARQLGroupPatternImpl implements SPARQLGroupPattern
{
	protected final List<SPARQLGraphPattern> subPatterns;

	public SPARQLGroupPatternImpl( final List<SPARQLGraphPattern> subPatterns ) {
		this.subPatterns = new ArrayList<>(subPatterns);
	}

	public SPARQLGroupPatternImpl( final SPARQLGraphPattern ... subPatterns ) {
		this.subPatterns = new ArrayList<>( Arrays.asList(subPatterns) );
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

	public void addSubPattern( final SPARQLGraphPattern p ) {
		subPatterns.add(p);
	}

	@Override
	public int hashCode() {
		return Objects.hash(subPatterns);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SPARQLGroupPatternImpl other = (SPARQLGroupPatternImpl) obj;
		return subPatterns.equals(other.subPatterns);
	}

}
