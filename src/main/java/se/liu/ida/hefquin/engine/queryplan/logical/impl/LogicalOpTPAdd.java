package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.TriplePattern;
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

	@Override
	public int hashCode(){
		return fm.hashCode() ^ tp.hashCode();
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
		final int codeOfTP = tp.toString().hashCode();
		final int codeOfFm = fm.getInterface().toString().hashCode();

		return "> tpAdd" +
				"[" + codeOfTP + ", "+ codeOfFm + "]"+
				" ( "
				+ tp.toString()
				+ ", "
				+ fm.getInterface().toString()
				+ " )";
	}

}
