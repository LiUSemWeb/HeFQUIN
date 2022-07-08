package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;

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

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof LogicalOpRequest) )
			return false;

		final LogicalOpRequest<?,?> oo = (LogicalOpRequest<?,?>) o;
		if ( oo == this )
			return true;
		else
			return oo.fm.equals(fm) && oo.req.equals(req); 
	}

	@Override
	public int hashCode(){
		return fm.hashCode() ^ req.hashCode();
	}

	public MemberType getFederationMember() {
		return fm;
	}

	public ReqType getRequest() {
		return req;
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		final int codeOfReq = req.toString().hashCode();
		final int codeOfFm = fm.getInterface().toString().hashCode();

		return "> req" +
				"[" + codeOfReq + ", "+ codeOfFm + "]"+
				" ( " +
				"{ " + req.toString() + " }"
				+ ", "
				+ fm.getInterface().toString()
				+ " )";
	}

}
