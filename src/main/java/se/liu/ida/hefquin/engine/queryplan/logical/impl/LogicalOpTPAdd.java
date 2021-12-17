package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalInterface;
import se.liu.ida.hefquin.engine.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
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

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof LogicalOpTPAdd) )
			return false;

		final LogicalOpTPAdd oo = (LogicalOpTPAdd) o;
		if ( oo == this )
			return true;
		else
			return oo.fm.equals(fm) && oo.tp.equals(tp); 
	}

	public FederationMember getFederationMember() {
		return fm;
	}

	public TriplePattern getTP() {
		return tp;
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		final StringBuilder builder = new StringBuilder();

		builder.append("> tpAdd ");
		builder.append("( ");
		builder.append( tp.asJenaTriple() );
		builder.append(", ");
		LogicalPlanUtils.printStringOfFm( builder, fm );
		builder.append(" )");

		return builder.toString();
	}

}
