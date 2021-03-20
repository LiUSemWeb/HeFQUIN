package se.liu.ida.hefquin.queryplan.logical.impl;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.query.TriplePattern;
import se.liu.ida.hefquin.queryplan.LogicalOperator;
import se.liu.ida.hefquin.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpTPAdd extends UnaryLogicalOpImpl
{
	protected final FederationMember fm;

	protected final TriplePattern tp;

	LogicalOpTPAdd( final LogicalOperator childOp, final FederationMember fm, final TriplePattern tp ) {
		super(childOp);

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
