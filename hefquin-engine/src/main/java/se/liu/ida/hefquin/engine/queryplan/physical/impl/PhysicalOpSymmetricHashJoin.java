package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpSymmetricHashJoin;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements the symmetric hash join algorithm
 * to perform an inner join of two sequences of input solution mappings
 * (produced by the two sub-plans under this operator).
 *
 * For a more detailed description of the algorithm, refer to {@link
 * ExecOpSymmetricHashJoin}, which provides the implementation of this
 * algorithm.
 */
public class PhysicalOpSymmetricHashJoin extends BaseForPhysicalOpBinaryJoin
{
    public PhysicalOpSymmetricHashJoin( final LogicalOpJoin lop ) {
        super(lop);
    }

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpSymmetricHashJoin
				&& ((PhysicalOpSymmetricHashJoin) o).lop.equals(lop);
	}

    @Override
    public BinaryExecutableOp createExecOp( final boolean collectExceptions,
                                            final QueryPlanningInfo qpInfo,
	                                        final ExpectedVariables ... inputVars ) {
        assert inputVars.length == 2;

        return new ExecOpSymmetricHashJoin( inputVars[0], inputVars[1], collectExceptions, qpInfo );
    }

    @Override
    public void visit(final PhysicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString(){
       return "> symmetricHashJoin ";
    }

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables inputVars ) {
			return lop instanceof LogicalOpJoin;
		}

		@Override
		public PhysicalOperator create( final LogicalOperator lop ) {
			if ( lop instanceof LogicalOpJoin ) {
				return new PhysicalOpSymmetricHashJoin( (LogicalOpJoin) lop);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}
}