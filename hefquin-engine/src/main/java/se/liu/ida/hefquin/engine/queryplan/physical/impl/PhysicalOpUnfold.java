package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpUnfold;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnfold;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

public class PhysicalOpUnfold implements UnaryPhysicalOpForLogicalOp
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	protected final LogicalOpUnfold lop;

	protected PhysicalOpUnfold( final LogicalOpUnfold lop ) {
		this.lop = lop;
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final QueryPlanningInfo qpInfo,
	                                       final ExpectedVariables ... inputVars ) {
		return new ExecOpUnfold( lop.getExpr(),
		                         lop.getVar1(),
		                         lop.getVar2(),
		                         collectExceptions,
		                         qpInfo,
		                         lop.mayReduce() );
	}

	@Override
	public LogicalOpUnfold getLogicalOperator() {
		return lop;
 	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return    o instanceof PhysicalOpUnfold oo
		       && oo.lop.equals(lop);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ lop.hashCode();
	}

	@Override
	public String toString() {
		return lop.toString();
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			return ( lop instanceof LogicalOpUnfold );
		}

		@Override
		public PhysicalOpUnfold create( final UnaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpUnfold op ) {
				return new PhysicalOpUnfold(op);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}
}
