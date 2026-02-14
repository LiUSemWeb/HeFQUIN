package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;

public class LogicalOpRequest<ReqType extends DataRetrievalRequest,
                              MemberType extends FederationMember>
		implements NullaryLogicalOp
{
	protected final MemberType fm;
	protected final ReqType req;

	public LogicalOpRequest( final MemberType fm, final ReqType req ) {
		assert fm != null;
		assert fm != req;

		this.fm = fm;
		this.req = req;
	}

	public MemberType getFederationMember() {
		return fm;
	}

	public ReqType getRequest() {
		return req;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 0;

		return req.getExpectedVariables();
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		return (    o instanceof LogicalOpRequest oo
		         && oo.fm.equals(fm)
		         && oo.req.equals(req) );
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ fm.hashCode() ^ req.hashCode();
	}

	@Override
	public String toString() {
		return "req (fm: " + fm.hashCode() + ", req: " + req.hashCode() + ")";
	}
}
