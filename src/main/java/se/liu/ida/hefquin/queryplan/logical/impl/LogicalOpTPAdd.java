package se.liu.ida.hefquin.queryplan.logical.impl;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.query.TriplePattern;
import se.liu.ida.hefquin.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.queryplan.logical.UnaryLogicalOp;

public class LogicalOpTPAdd implements UnaryLogicalOp
{
	protected final FederationMember fm;

	protected final TriplePattern tp;

	LogicalOpTPAdd( final FederationMember fm, final TriplePattern tp ) {
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

	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
