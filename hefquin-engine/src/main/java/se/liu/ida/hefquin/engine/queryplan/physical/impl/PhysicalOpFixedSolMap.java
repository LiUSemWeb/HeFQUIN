package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Objects;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.NullaryExecutableOpBase;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFixedSolMap;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * A physical operator that returns a given solution mapping.
 */
public class PhysicalOpFixedSolMap implements NullaryPhysicalOpForLogicalOp
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	protected final LogicalOpFixedSolMap lop;

	protected PhysicalOpFixedSolMap( final LogicalOpFixedSolMap lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		return o instanceof PhysicalOpFixedSolMap in && in.lop.equals(lop);
	}

	@Override
	public int hashCode(){
		return lop.hashCode() ^ Objects.hash( this.getClass().getName() );
	}


	@Override
	public LogicalOpFixedSolMap getLogicalOperator() {
		return lop;
	}

	@Override
	public NullaryExecutableOp createExecOp( final boolean collectExceptions,
	                                         final QueryPlanningInfo qpInfo,
	                                         final ExpectedVariables ... inputVars ) {
		return new NullaryExecutableOpBase(collectExceptions, qpInfo) {
			@Override
			protected void _execute( final IntermediateResultElementSink sink,
			                         final ExecutionContext execCxt ) {
				sink.send( lop.getSolutionMapping() );
			}
		};
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return lop.toString();
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			return lop instanceof LogicalOpFixedSolMap;
		}

		@Override
		public PhysicalOpFixedSolMap create( final NullaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpFixedSolMap op ) {
				return new PhysicalOpFixedSolMap(op);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}

}
