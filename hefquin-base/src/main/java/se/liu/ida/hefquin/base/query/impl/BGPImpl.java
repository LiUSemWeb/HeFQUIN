package se.liu.ida.hefquin.base.query.impl;

import java.util.*;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.TriplePattern;

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
			builder.append( " ." );
		}
		builder.append( " )");

		return builder.toString();
	}

	@Override
	public boolean equals( final Object o ) {
		if (this == o) return true;
		if ( !(o instanceof BGP) ) return false;

		if ( o instanceof BGPImpl ) {
			final BGPImpl bgp = (BGPImpl) o;
			return Objects.equals(tps, bgp.tps);
		}
		else {
			final BGP bgp = (BGP) o;
			final Set<? extends TriplePattern> tpsOther = bgp.getTriplePatterns();
			return (tps.size() == tpsOther.size()) && tps.containsAll(tpsOther);
		}
	}

	@Override
	public int hashCode() {
		int code = Objects.hash( super.getClass().getName() );
		for( TriplePattern tp: tps){
			code = code ^ tp.hashCode();
		}
		return code;
	}
}
