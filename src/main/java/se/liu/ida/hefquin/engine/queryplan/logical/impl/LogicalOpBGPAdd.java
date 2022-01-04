package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpBGPAdd implements UnaryLogicalOp
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

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof LogicalOpBGPAdd) )
			return false;

		final LogicalOpBGPAdd oo = (LogicalOpBGPAdd) o;
		if ( oo == this )
			return true;
		else
			return oo.fm.equals(fm) && oo.bgp.equals(bgp); 
	}

	public FederationMember getFederationMember() {
		return fm;
	} 

	public BGP getBGP() {
		return bgp;
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){

		return "> bgpAdd ( "
				+ bgp.toString()
				+ ", "
				+ fm.getInterface().toString()
				+ " )";
	}

}
