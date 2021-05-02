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
