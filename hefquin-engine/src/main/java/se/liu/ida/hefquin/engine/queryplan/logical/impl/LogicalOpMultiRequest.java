package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.Set;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;

public class LogicalOpMultiRequest extends BaseForLogicalOps
                                   implements NullaryLogicalOp
{
	protected final SPARQLRequest req;
	protected final Set<FederationMember> fms;

	public LogicalOpMultiRequest( final SPARQLRequest req,
	                              final Set<FederationMember> fms ) {
		super( req.getDistinctRequired() );

		assert req != null;
		assert fms != null;

		this.req = req;
		this.fms = fms;
	}

	public SPARQLRequest getRequest() {
		return req;
	}

	public Set<FederationMember> getFederationMembers() {
		return fms;
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

		return (    o instanceof LogicalOpMultiRequest oo
		         && oo.req.equals(req)
		         && oo.fms.equals(fms) );
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ req.hashCode() ^ fms.hashCode();
	}

	@Override
	public String toString() {
		return "mreq (req: " + req.hashCode() + ", fms: " + fms.hashCode() + ")";
	}
}
