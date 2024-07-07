package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpNaiveNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * Attention, this is not actually a (distributed) nested loops join;
 * see {@link ExecOpNaiveNestedLoopsJoin} for more details.
 */
public class PhysicalOpNaiveNestedLoopsJoin extends BaseForPhysicalOpBinaryJoin
{
    public PhysicalOpNaiveNestedLoopsJoin( final LogicalOpJoin lop ) {
        super(lop);
    }

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpNaiveNestedLoopsJoin && ((PhysicalOpNaiveNestedLoopsJoin) o).lop.equals(lop);
	}

    @Override
    public BinaryExecutableOp createExecOp( final boolean collectExceptions,
                                            final ExpectedVariables... inputVars ) {
        return new ExecOpNaiveNestedLoopsJoin(collectExceptions);
    }

    @Override
    public void visit( final PhysicalPlanVisitor visitor ) {
        visitor.visit(this);
    }

    @Override
    public String toString(){
        return "> naiveNestedLoop ";
    }

}
