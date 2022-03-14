package se.liu.ida.hefquin.engine.query.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Triple;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;

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
		//System.out.print("In Union equals: " + other.toString() + ", " + subPatterns + "\n");
		if ( this == other )
			return true;

		if ( other == null || !(other instanceof SPARQLUnionPattern) )
			return false;
		
		/*
		final Set<SPARQLGraphPattern> thisTriple = new HashSet<>();
		final Set<SPARQLGraphPattern> otherTriple = new HashSet<>();
		
		for(SPARQLGraphPattern i : subPatterns) {
			thisTriple.add(i);
		}
		
		for(SPARQLGraphPattern j : ((SPARQLUnionPattern) other).getSubPatterns()) {
			otherTriple.add(j);
		}
		
		return thisTriple.equals(otherTriple);
		*/
		
		return subPatterns.containsAll((Collection<?>) ((SPARQLUnionPattern) other).getSubPatterns());
	}

}
