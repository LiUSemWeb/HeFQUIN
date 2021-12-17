package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpUnion;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalInterface;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
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
		final StringBuilder builder = new StringBuilder();

		builder.append(" > req ");
		builder.append("( ");

		builder.append("{ ");
		if ( req instanceof SPARQLRequest) {
			final SPARQLGraphPattern pattern = ((SPARQLRequest) req).getQueryPattern();
			if ( pattern instanceof TriplePattern) {
				builder.append( ((TriplePattern)pattern).asJenaTriple() );
			}
			else if ( pattern instanceof BGP) {
				builder.append( ((BGP) pattern).getTriplePatterns() );
			}
			else {
				LogicalPlanUtils.printTriplesOfGraphPattern( pattern.asJenaOp(), builder );
			}
		}
		else {
			throw new UnsupportedOperationException( "Print graph pattern of the Request: " + req.getClass().getName()+" is an open TODO." );
		}
		builder.append(" }");

		builder.append(", ");
		LogicalPlanUtils.printStringOfFm( builder, fm );
		builder.append(" )");

		return builder.toString();
	}

}
