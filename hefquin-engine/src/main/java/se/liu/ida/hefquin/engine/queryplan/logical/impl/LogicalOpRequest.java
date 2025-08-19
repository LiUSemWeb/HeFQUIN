package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlanOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;

public class LogicalOpRequest<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
                        extends BaseForQueryPlanOperator
                        implements NullaryLogicalOp
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
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 0;

		return req.getExpectedVariables();
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		return "req" + " (" + getID() + ")"
		  		+ "\t - fm (" + fm.getInterface().toString() + ")"
		  		+ "\t - pattern (" + req.toString() + ")";
		
	}

}
