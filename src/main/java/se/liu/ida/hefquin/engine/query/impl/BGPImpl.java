package se.liu.ida.hefquin.engine.query.impl;

import java.util.*;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BGPImpl)) return false;
		BGPImpl bgp = (BGPImpl) o;
		return Objects.equals(tps, bgp.tps);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tps);
	}
}
