package se.liu.ida.hefquin.queryplan.logical.impl;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.query.BGP;
import se.liu.ida.hefquin.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.queryplan.logical.UnaryLogicalOp;

public class LogicalOpBGPAdd implements UnaryLogicalOp
{
	protected final FederationMember fm;

	protected final BGP bgp;

	LogicalOpBGPAdd( final FederationMember fm, final BGP bgp ) {
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

	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
