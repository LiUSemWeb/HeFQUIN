package se.liu.ida.hefquin.base.query.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLGroupPattern;

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
	public String toString(){
		final int size = getNumberOfSubPatterns();

		final StringBuilder builder = new StringBuilder();
		builder.append( "(SPARQLGroupPattern ");
		for ( int i = 0; i < size; i ++ ){
			builder.append( getSubPatterns(i).toString() );
			if( i < size-1 ) {
				builder.append(" AND ");
			}
		}
		builder.append( " )");

		return builder.toString();
	}

	@Override
	public boolean equals( final Object other ) {
		if ( this == other )
			return true;

		if ( other == null || !(other instanceof SPARQLGroupPattern) )
			return false;

		return ((SPARQLGroupPattern) other).getSubPatterns().equals(subPatterns);
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
