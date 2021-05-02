package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpTPAdd implements UnaryLogicalOp
{
	protected final FederationMember fm;

	protected final TriplePattern tp;

	public LogicalOpTPAdd( final FederationMember fm, final TriplePattern tp ) {
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
