package se.liu.ida.hefquin.queryplan.logical;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;

public class LogicalOpRequest<ReqType extends DataRetrievalRequest> extends NullaryLogicalOpImpl
{
	protected final FederationMember fm;
	protected final ReqType req;

	LogicalOpRequest( final FederationMember fm, final ReqType req ) {
		assert fm != null;
		assert fm != req;
		assert fm.getInterface().supportsRequest(req);

		this.fm = fm;
		this.req = req;
	}

	public FederationMember getFederationMember() {
		return fm;
	}

	public ReqType getRequest() {
		return req;
	}

	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
