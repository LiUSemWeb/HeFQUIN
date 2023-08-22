package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Objects;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

public abstract class BasePhysicalOpSingleInputJoin implements UnaryPhysicalOpForLogicalOp
{
    protected final UnaryLogicalOp lop;

    /**
     * The given logical operator is expected to be of one of the following
     * six types:
     * {@link LogicalOpTPAdd}, {@link LogicalOpTPOptAdd},
     * {@link LogicalOpBGPAdd}, {@link LogicalOpBGPOptAdd},
     * {@link LogicalOpGPAdd}, or {@link LogicalOpGPOptAdd}.
     */
    protected BasePhysicalOpSingleInputJoin( final UnaryLogicalOp lop ) {
        assert lop != null;
        assert (lop instanceof LogicalOpBGPAdd) || (lop instanceof LogicalOpBGPOptAdd) ||
               (lop instanceof LogicalOpTPAdd) || (lop instanceof LogicalOpTPOptAdd) ||
               (lop instanceof LogicalOpGPAdd) || (lop instanceof LogicalOpGPOptAdd);
        this.lop = lop;
    }

	@Override
	public boolean equals( final Object o ) {
		return o instanceof UnaryPhysicalOpForLogicalOp
				&& ((UnaryPhysicalOpForLogicalOp) o).getLogicalOperator().equals(lop);
	}

	@Override
	public int hashCode(){
		return lop.hashCode() ^ Objects.hash( this.getClass().getName() );
	}

    @Override
    public UnaryLogicalOp getLogicalOperator() { return lop; }

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		return lop.getExpectedVariables(inputVars);
	}

}
