package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpGPOptAdd implements UnaryLogicalOp
{
	protected final FederationMember fm;
	protected final SPARQLGraphPattern pattern;

	public LogicalOpGPOptAdd( final FederationMember fm, final SPARQLGraphPattern pattern ) {
		assert fm != null;
		assert pattern != null;
		assert fm.getInterface().supportsSPARQLPatternRequests();

		this.fm = fm;
		this.pattern = pattern;
	}

	public FederationMember getFederationMember() {
		return fm;
	} 

	public SPARQLGraphPattern getPattern() {
		return pattern;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		if ( ! (o instanceof LogicalOpGPOptAdd) )
			return false;

		final LogicalOpGPOptAdd oo = (LogicalOpGPOptAdd) o;
		return oo.fm.equals(fm) && oo.pattern.equals(pattern); 
	}

	@Override
	public int hashCode(){
		return fm.hashCode() ^ pattern.hashCode();
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		final int codeOfPattern = pattern.toString().hashCode();
		final int codeOfFm = fm.getInterface().toString().hashCode();

		return "> gpOptAdd" +
				"[" + codeOfPattern + ", "+ codeOfFm + "]"+
				" ( "
				+ pattern.toString()
				+ ", "
				+ fm.getInterface().toString()
				+ " )";
	}

}
