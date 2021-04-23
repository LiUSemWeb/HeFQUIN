package se.liu.ida.hefquin.queryplan.logical.impl;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.queryplan.logical.NullaryLogicalOp;

public class LogicalOpRequest<ReqType extends DataRetrievalRequest, MemberType extends FederationMember> implements NullaryLogicalOp
{
	protected final MemberType fm;
	protected final ReqType req;

	public LogicalOpRequest( final MemberType fm, final ReqType req ) {
		assert fm != null;
		assert fm != req;
		assert fm.getInterface().supportsRequest(req);

		this.fm = fm;
		this.req = req;
	}

	public MemberType getFederationMember() {
		return fm;
	}

	public ReqType getRequest() {
		return req;
	}

	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
