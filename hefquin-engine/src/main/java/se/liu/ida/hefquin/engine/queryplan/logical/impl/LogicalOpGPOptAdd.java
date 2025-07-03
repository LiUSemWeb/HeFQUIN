package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.federation.FederationMember;

public class LogicalOpGPOptAdd extends LogicalOperatorBase implements UnaryLogicalOp
{
	protected final FederationMember fm;
	protected final SPARQLGraphPattern pattern;

	// will be initialized on demand 
	protected TriplePattern tp = null;
	protected boolean tpCheckDone = false;

	public LogicalOpGPOptAdd( final FederationMember fm, final SPARQLGraphPattern pattern ) {
		assert fm != null;
		assert pattern != null;

		this.fm = fm;
		this.pattern = pattern;
	}

	public FederationMember getFederationMember() {
		return fm;
	}

	public SPARQLGraphPattern getPattern() {
		if ( tp != null )
			return tp;
		else
			return pattern;
	}

	/**
	 * Returns <code>true</code> if the graph pattern
	 * of this operator is only a triple pattern.
	 */
	public boolean containsTriplePatternOnly() {
		if ( ! tpCheckDone ) {
			tpCheckDone = true;
			tp = QueryPatternUtils.getAsTriplePattern(pattern);
		}

		return ( tp != null );
	}

	/**
	 * Returns the graph pattern of this operator as a triple pattern if this
	 * pattern is only a triple pattern.
	 * <p>
	 * Before calling this function, use {@link #containsTriplePatternOnly()}
	 * to check whether the pattern is indeed only a triple pattern.
	 * <p>
	 * If it is not, this method throws an {@link UnsupportedOperationException}.
	 */
	public TriplePattern getTP() {
		if ( ! containsTriplePatternOnly() ) {
			throw new UnsupportedOperationException("This graph pattern is not a triple pattern");
		}

		return tp;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		final ExpectedVariables expVarsPattern = pattern.getExpectedVariables();
		final ExpectedVariables expVarsInput = inputVars[0];

		final Set<Var> certainVars = expVarsInput.getCertainVariables();

		final Set<Var> possibleVars = new HashSet<>();
		possibleVars.addAll( expVarsPattern.getCertainVariables() );
		possibleVars.addAll( expVarsPattern.getPossibleVariables() );
		possibleVars.addAll( expVarsInput.getPossibleVariables() );
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		if ( ! (o instanceof LogicalOpGPOptAdd) )
			return false;

		final LogicalOpGPOptAdd oo = (LogicalOpGPOptAdd) o;
		return oo.fm.equals(fm) && oo.pattern.equals(pattern); 
	}

	@Override
	public int hashCode(){
		return fm.hashCode() ^ pattern.hashCode();
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		final int codeOfPattern = pattern.toString().hashCode();
		final int codeOfFm = fm.getInterface().toString().hashCode();

		return "> gpOptAdd" +
				"[" + codeOfPattern + ", "+ codeOfFm + "]"+
				" ( "
				+ pattern.toString()
				+ ", "
				+ fm.getInterface().toString()
				+ " )";
	}

}
