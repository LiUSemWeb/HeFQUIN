package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpGPAdd extends LogicalOperatorBase implements UnaryLogicalOp
{
	protected final FederationMember fm;
	protected final SPARQLGraphPattern pattern;

	public LogicalOpGPAdd( final FederationMember fm, final SPARQLGraphPattern pattern ) {
		assert fm != null;
		assert pattern != null;
		assert fm.getInterface().supportsSPARQLPatternRequests();

		this.fm = fm;
		this.pattern = pattern;
	}

	public FederationMember getFederationMember() {
		return fm;
	} 

	public SPARQLGraphPattern getPattern() {
		return pattern;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		final ExpectedVariables expVarsPattern = pattern.getExpectedVariables();
		final ExpectedVariables expVarsInput = inputVars[0];

		final Set<Var> certainVars = ExpectedVariablesUtils.unionOfCertainVariables(expVarsPattern, expVarsInput);
		final Set<Var> possibleVars = ExpectedVariablesUtils.unionOfPossibleVariables(expVarsPattern, expVarsInput);
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

		if ( ! (o instanceof LogicalOpGPAdd) )
			return false;

		final LogicalOpGPAdd oo = (LogicalOpGPAdd) o;
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

		return "> gpAdd" +
				"[" + codeOfPattern + ", "+ codeOfFm + "]"+
				" ( "
				+ pattern.toString()
				+ ", "
				+ fm.getInterface().toString()
				+ " )";
	}

}
