package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpBGPAdd extends LogicalOperatorBase implements UnaryLogicalOp
{
	protected final FederationMember fm;
	protected final BGP bgp;

	public LogicalOpBGPAdd( final FederationMember fm, final BGP bgp ) {
		assert fm != null;
		assert bgp != null;
		assert fm.getInterface().supportsBGPRequests();

		this.fm = fm;
		this.bgp = bgp;
	}

	public FederationMember getFederationMember() {
		return fm;
	} 

	public BGP getBGP() {
		return bgp;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		final ExpectedVariables expVarsPattern = bgp.getExpectedVariables();
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

		if ( ! (o instanceof LogicalOpBGPAdd) )
			return false;

		final LogicalOpBGPAdd oo = (LogicalOpBGPAdd) o;
		return oo.fm.equals(fm) && oo.bgp.equals(bgp); 
	}

	@Override
	public int hashCode(){
		return fm.hashCode() ^ bgp.hashCode();
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		final int codeOfBGP = bgp.toString().hashCode();
		final int codeOfFm = fm.getInterface().toString().hashCode();

		return "> bgpAdd" +
				"[" + codeOfBGP + ", "+ codeOfFm + "]"+
				" ( "
				+ bgp.toString()
				+ ", "
				+ fm.getInterface().toString()
				+ " )";
	}

}
