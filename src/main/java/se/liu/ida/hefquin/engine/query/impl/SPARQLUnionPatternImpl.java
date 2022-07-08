package se.liu.ida.hefquin.engine.query.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;

public class SPARQLUnionPatternImpl implements SPARQLUnionPattern
{
	protected final List<SPARQLGraphPattern> subPatterns;

	public SPARQLUnionPatternImpl( final List<SPARQLGraphPattern> subPatterns ) {
		this.subPatterns = new ArrayList<>(subPatterns);
	}

	public SPARQLUnionPatternImpl( final SPARQLGraphPattern ... subPatterns ) {
		this.subPatterns = new ArrayList<>( Arrays.asList(subPatterns) );
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
	public boolean equals( final Object other ) {
		if ( this == other )
			return true;

		if ( other == null || !(other instanceof SPARQLUnionPattern) )
			return false;
		
		return subPatterns.equals(((SPARQLUnionPattern) other).getSubPatterns());
	}

	@Override
	public int hashCode() {
		int code = Objects.hash( super.getClass().getName() );
		for( SPARQLGraphPattern p: subPatterns){
			code = code ^ p.hashCode();
		}
		return code;
	}

}
