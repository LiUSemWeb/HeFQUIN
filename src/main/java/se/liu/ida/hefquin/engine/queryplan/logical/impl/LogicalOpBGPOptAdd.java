package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpBGPOptAdd implements UnaryLogicalOp
{
	protected final FederationMember fm;
	protected final BGP bgp;

	public LogicalOpBGPOptAdd( final FederationMember fm, final BGP bgp ) {
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
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		if ( ! (o instanceof LogicalOpBGPOptAdd) )
			return false;

		final LogicalOpBGPOptAdd oo = (LogicalOpBGPOptAdd) o;
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

		return "> bgpOptAdd" +
				"[" + codeOfBGP + ", "+ codeOfFm + "]"+
				" ( "
				+ bgp.toString()
				+ ", "
				+ fm.getInterface().toString()
				+ " )";
	}

}
