package se.liu.ida.hefquin.base.query.impl;

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;

public class BGPImpl implements BGP
{
	private final Set<TriplePattern> tps;

	public BGPImpl() {
		this.tps = new HashSet<>();
	}

	public BGPImpl( final Set<TriplePattern> tps ) {
		this.tps = new HashSet<>(tps);
	}

	public BGPImpl( final TriplePattern tp, final TriplePattern ... tps ) {
		this.tps = new HashSet<>();
		this.tps.add(tp);
		this.tps.addAll( Arrays.asList(tps) );
	}

	public BGPImpl( final BGP otherBGP, final BGP ... otherBGPs ) {
		tps = new HashSet<>( otherBGP.getTriplePatterns() );
		for ( final BGP anotherBGP : otherBGPs ) {
			tps.addAll( anotherBGP.getTriplePatterns() );
		}
	}

	public BGPImpl( final TriplePattern tp, final BGP otherBGP ) {
		tps = new HashSet<>();
		tps.add(tp);
		tps.addAll( otherBGP.getTriplePatterns() );
	}

	public BGPImpl( final BGP otherBGP, final TriplePattern tp ) {
		tps = new HashSet<>();
		tps.addAll( otherBGP.getTriplePatterns() );
		tps.add(tp);
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

	@Override
	public Set<TriplePattern> getAllMentionedTPs() {
		return getTriplePatterns();
	}

	@Override
	public Set<Var> getCertainVariables() {
		return getAllMentionedVariables();
	}

	@Override
	public Set<Var> getPossibleVariables() {
		return Collections.emptySet();
	}

	@Override
	public Set<Var> getAllMentionedVariables() {
		// To reduce the number of HashSet objects being created,
		// this implementation inspects the triple patterns directly
		// instead of simply using their getAllMentionedVariables()
		// method.
		final Set<Var> vars = new HashSet<>();
		for ( final TriplePattern tp : tps ) {
			final Node s = tp.asJenaTriple().getSubject();
			final Node p = tp.asJenaTriple().getPredicate();
			final Node o = tp.asJenaTriple().getObject();

			if ( Var.isVar(s) ) { vars.add( Var.alloc(s) ); }
			if ( Var.isVar(p) ) { vars.add( Var.alloc(p) ); }
			if ( Var.isVar(o) ) { vars.add( Var.alloc(o) ); }
		}

		return vars;
	}

	@Override
	public int getNumberOfVarMentions() {
		int n = 0;
		for ( final TriplePattern tp : tps ) {
			n += tp.getNumberOfVarMentions();
		}

		return n;
	}

	@Override
	public int getNumberOfTermMentions() {
		int n = 0;
		for ( final TriplePattern tp : tps ) {
			n += tp.getNumberOfTermMentions();
		}

		return n;
	}

	@Override
	public BGP applySolMapToGraphPattern( final SolutionMapping sm )
			throws VariableByBlankNodeSubstitutionException
	{
		final Set<TriplePattern> tps = new HashSet<>();
		boolean unchanged = true;

		for ( final TriplePattern tp : tps ) {
			final TriplePattern tp2 = tp.applySolMapToGraphPattern(sm);
			tps.add(tp2);

			if ( tp != tp2 ) unchanged = false;
		}

		return unchanged ? this : new BGPImpl(tps);
	}

	@Override
	public SPARQLGraphPattern mergeWith( final SPARQLGraphPattern other ) {
		if ( other instanceof TriplePattern tp )
			return mergeWith(tp);

		if ( other instanceof BGP bgp ) {
			return mergeWith(bgp);
		}

		final Element elmt = QueryPatternUtils.convertToJenaElement(other);
		return QueryPatternUtils.merge(this, elmt);
	}

	@Override
	public BGP mergeWith( final TriplePattern tp ) {
		return new BGPImpl(this, tp);
	}

	@Override
	public BGP mergeWith( final BGP otherBGP ) {
		return new BGPImpl(this, otherBGP);
	}

}
