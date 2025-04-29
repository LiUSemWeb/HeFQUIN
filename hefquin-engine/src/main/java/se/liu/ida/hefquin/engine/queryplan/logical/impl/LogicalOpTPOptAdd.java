package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpTPOptAdd extends LogicalOperatorBase implements UnaryLogicalOp
{
	protected final FederationMember fm;
	protected final TriplePattern tp;

	public LogicalOpTPOptAdd( final FederationMember fm, final TriplePattern tp ) {
		assert fm != null;
		assert tp != null;
		assert fm.getInterface().supportsTriplePatternRequests();

		this.fm = fm;
		this.tp = tp;
	}

	public FederationMember getFederationMember() {
		return fm;
	}

	public TriplePattern getTP() {
		return tp;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		final ExpectedVariables expVarsPattern = tp.getExpectedVariables();
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

		if ( ! (o instanceof LogicalOpTPOptAdd) )
			return false;

		final LogicalOpTPOptAdd oo = (LogicalOpTPOptAdd) o;
		return oo.fm.equals(fm) && oo.tp.equals(tp); 
	}

	@Override
	public int hashCode(){
		return fm.hashCode() ^ tp.hashCode();
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		final int codeOfTP = tp.toString().hashCode();
		final int codeOfFm = fm.getInterface().toString().hashCode();

		return "> tpOptAdd" +
				"[" + codeOfTP + ", "+ codeOfFm + "]"+
				" ( "
				+ tp.toString()
				+ ", "
				+ fm.getInterface().toString()
				+ " )";
	}
	
}
