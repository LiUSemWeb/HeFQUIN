package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;

public class LogicalOpMultiRequest extends BaseForLogicalOps
                                   implements NullaryLogicalOp
{
	protected final SPARQLRequest req;
	protected final Var var;
	protected final Set<FederationMember> fms;

	public LogicalOpMultiRequest( final SPARQLRequest req,
	                              final Var var, 
	                              final Set<FederationMember> fms ) {
		super( req.getDistinctRequired() );

		assert req != null;
		assert var != null;
		assert fms != null;

		this.req = req;
		this.var = var;
		this.fms = fms;
	}

	public SPARQLRequest getRequest() {
		return req;
	}

	public Var getServiceVariable() {
		return var;
	}

	public Set<FederationMember> getFederationMembers() {
		return fms;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 0;

		// The expected variables of a multi-request operator are the
		// expected variables of the request pattern, plus the service
		// variable as another certain variable.

		final ExpectedVariables reqEVars = req.getExpectedVariables();
		final Set<Var> reqCVars = reqEVars.getCertainVariables();
		final Set<Var> reqPVars = reqEVars.getPossibleVariables();

		if ( reqCVars.contains(var) )
			return reqEVars;

		final Set<Var> myCertainVars = new HashSet<>(reqCVars);
		myCertainVars.add(var);

		final Set<Var> myPossibleVars;
		if ( reqPVars.contains(var) ) {
			myPossibleVars = new HashSet<>(reqPVars);
			myPossibleVars.remove(var);
		}
		else {
			myPossibleVars = reqPVars;
		}

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return myCertainVars; }
			@Override public Set<Var> getPossibleVariables() { return myPossibleVars; }
		};
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
		         && oo.var.equals(var)
		         && oo.fms.equals(fms) );
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ req.hashCode() ^ var.hashCode() ^ fms.hashCode();
	}

	@Override
	public String toString() {
		return "mreq (var: " + var.toString() + ", req: " + req.hashCode() + ", fms: " + fms.hashCode() + ")";
	}
}
