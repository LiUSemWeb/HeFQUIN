package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlanOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpFilter;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

/**
 * A physical operator that filters the input solution mappings.
 *
 * The actual algorithm of this operator is implemented in the
 * {@link ExecOpFilter} class.
 */
public class PhysicalOpFilter extends BaseForQueryPlanOperator
                              implements UnaryPhysicalOpForLogicalOp
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	protected final LogicalOpFilter lop;

	protected PhysicalOpFilter( final LogicalOpFilter lop ) {
		this.lop = lop;
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final QueryPlanningInfo qpInfo,
	                                       final ExpectedVariables... inputVars ) {
		return new ExecOpFilter( lop.getFilterExpressions(), collectExceptions, qpInfo );
	}

	@Override
	public LogicalOpFilter getLogicalOperator() {
		return lop;
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			return ( lop instanceof LogicalOpFilter );
		}

		@Override
		public PhysicalOpFilter create( final UnaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpFilter op ) {
				return new PhysicalOpFilter(op);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}
}
