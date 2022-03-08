package se.liu.ida.hefquin.engine.query.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

		/*
		Set<SPARQLGraphPattern> otherGp = new HashSet<>();
		for (SPARQLGraphPattern gp : ((SPARQLUnionPattern) other).getSubPatterns()) {
			otherGp.add(gp);
		}
		
		Set<SPARQLGraphPattern> thisGp = new HashSet<>();
		for (SPARQLGraphPattern gp : subPatterns) {
			thisGp.add(gp);
		}
		
		System.out.print(otherGp + "\n");
		System.out.print(thisGp + "\n");
		return otherGp.equals(thisGp);
		*/
		return ((SPARQLUnionPattern) other).getSubPatterns().equals(subPatterns);
	}

}
