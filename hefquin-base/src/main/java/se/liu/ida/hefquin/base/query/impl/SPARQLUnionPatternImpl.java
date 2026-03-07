package se.liu.ida.hefquin.base.query.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.base.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;

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
	public String toString(){
		final int size = getNumberOfSubPatterns();

		final StringBuilder builder = new StringBuilder();
		builder.append( "(SPARQLUnionPattern ");
		for ( int i = 0; i < size; i ++ ){
			builder.append( getSubPatterns(i).toString() );
			if( i < size-1 ) {
				builder.append(" UNION ");
			}
		}
		builder.append( " )");

		return builder.toString();
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
		for( SPARQLGraphPattern p: subPatterns ) {
			code = code ^ p.hashCode();
		}
		return code;
	}

	@Override
	public Set<TriplePattern> getAllMentionedTPs() {
		if ( subPatterns.size() == 1 )
			return subPatterns.get(0).getAllMentionedTPs();

		final Set<TriplePattern> tps = new HashSet<>();
		for ( final SPARQLGraphPattern p : subPatterns ) {
			tps.addAll( p.getAllMentionedTPs() );
		}

		return tps;
	}

	@Override
	public Set<Var> getCertainVariables() {
		if ( subPatterns.size() == 1 )
			return subPatterns.get(0).getCertainVariables();

		final Set<Var> vars = new HashSet<>();
		for ( final SPARQLGraphPattern p : subPatterns ) {
			vars.addAll( p.getCertainVariables() );
		}

		return vars;
	}

	@Override
	public Set<Var> getPossibleVariables() {
		if ( subPatterns.size() == 1 )
			return subPatterns.get(0).getPossibleVariables();

		final Set<Var> vars = new HashSet<>();
		for ( final SPARQLGraphPattern p : subPatterns ) {
			vars.addAll( p.getPossibleVariables() );
		}

		return vars;
	}

	@Override
	public Set<Var> getAllMentionedVariables() {
		if ( subPatterns.size() == 1 )
			return subPatterns.get(0).getAllMentionedVariables();

		final Set<Var> vars = new HashSet<>();
		for ( final SPARQLGraphPattern p : subPatterns ) {
			vars.addAll( p.getAllMentionedVariables() );
		}

		return vars;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		if ( subPatterns.size() == 1 )
			return subPatterns.get(0).getExpectedVariables();

		final ExpectedVariables[] array = ExpectedVariablesUtils.getExpectedVariables(subPatterns);
		final Set<Var> certainVars = ExpectedVariablesUtils.intersectionOfCertainVariables(array);
		final Set<Var> possibleVars = ExpectedVariablesUtils.unionOfAllVariables(array);
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public int getNumberOfVarMentions() {
		int n = 0;
		for ( final SPARQLGraphPattern p : subPatterns ) {
			n += p.getNumberOfVarMentions();
		}

		return n;
	}

	@Override
	public int getNumberOfTermMentions() {
		int n = 0;
		for ( final SPARQLGraphPattern p : subPatterns ) {
			n += p.getNumberOfTermMentions();
		}

		return n;
	}

	/**
	 * If the given pattern is a {@link SPARQLGroupPattern}, then this method
	 * returns a {@link SPARQLGroupPattern} that consists of this pattern plus
	 * all the sub-patterns of the given {@link SPARQLGroupPattern}. Otherwise,
	 * this method returns a {@link SPARQLGroupPattern} that consists of this
	 * pattern plus the other given pattern (whatever it is).
	 */
	@Override
	public SPARQLGraphPattern mergeWith( final SPARQLGraphPattern other ) {
		if ( other instanceof SPARQLGroupPattern grp ) {
			final List<SPARQLGraphPattern> gps = new ArrayList<>( 1 + grp.getNumberOfSubPatterns() );
			gps.add(this);
			for ( final SPARQLGraphPattern p : grp.getSubPatterns() ) {
				gps.add(p);
			}
			return new SPARQLGroupPatternImpl(gps);
		}

		return new SPARQLGroupPatternImpl(this, other);
	}

	@Override
	public SPARQLUnionPattern applySolMapToGraphPattern( final Binding sm )
			throws VariableByBlankNodeSubstitutionException
	{
		final SPARQLUnionPatternImpl upNew = new SPARQLUnionPatternImpl();
		boolean unchanged = true;

		for ( final SPARQLGraphPattern p : subPatterns ) {
			final SPARQLGraphPattern pNew = p.applySolMapToGraphPattern(sm);
			upNew.addSubPattern(pNew);
			if ( ! pNew.equals(p) ) {
				unchanged = false;
			}
		}

		return ( unchanged ) ? this : upNew;
	}

	@Override
	public String toStringForPlanPrinters() {
		final StringBuilder b = new StringBuilder();
		for ( int i = 0; i < subPatterns.size(); i++ ) {
			if ( i > 0 ) {
				b.append( "UNION" );
				b.append( System.lineSeparator() );
			}
			b.append( "{" );
			b.append( System.lineSeparator() );
			b.append( subPatterns.get(i).toStringForPlanPrinters() );
			b.append( System.lineSeparator() );
			b.append( "}" );
		}

		return b.toString();
	}

}
