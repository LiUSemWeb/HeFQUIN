package se.liu.ida.hefquin.engine.query.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Triple;

import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class BGPImpl implements BGP
{
	private final Set<TriplePattern> tps;

	public BGPImpl( final Set<TriplePattern> tps ) {
		this.tps = new HashSet<>(tps);
	}

	public BGPImpl( final TriplePattern ... tps ) {
		this.tps = new HashSet<>( Arrays.asList(tps) );
	}

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof BGP) )
			return false;

		final Set<? extends TriplePattern> otps;
		if ( o instanceof BGPImpl )
			otps = ((BGPImpl) o).tps;
		else
			otps = ((BGP) o).getTriplePatterns();
		
		final Set<Triple> tpsTriple = new HashSet<>();
		final Set<Triple> otpsTriple = new HashSet<>();
		
		for(TriplePattern i : tps) {
			tpsTriple.add(i.asJenaTriple());
		}
		
		for(TriplePattern j : otps) {
			otpsTriple.add(j.asJenaTriple());
		}
		
		return tpsTriple.equals(otpsTriple)
		else
			return tps.containsAll(otps);
	}

	@Override
	public Set<TriplePattern> getTriplePatterns() {
		return Collections.unmodifiableSet(tps);
	}

	public void addTriplePattern( final TriplePattern tp ) {
		tps.add(tp);
	}


	@Override
	public String toString(){
		final StringBuilder builder = new StringBuilder();

		builder.append( "(bgp ");
		for ( TriplePattern tp: tps){
			builder.append( tp.toString() );
		}
		builder.append( " )");

		return builder.toString();
	}
}
