package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Objects;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOpForLogicalOp;

/**
 * Base class for physical operators that implement some form of a join
 * algorithm that consumes only one input and produces the other input
 * as part of the algorithm itself.
 */
public abstract class BaseForPhysicalOpSingleInputJoin extends BaseForPhysicalOps implements UnaryPhysicalOpForLogicalOp
{
    protected final UnaryLogicalOp lop;

    /**
     * The given logical operator is expected to be of one of the following
     * six types:
     * {@link LogicalOpTPAdd}, {@link LogicalOpTPOptAdd},
     * {@link LogicalOpBGPAdd}, {@link LogicalOpBGPOptAdd},
     * {@link LogicalOpGPAdd}, or {@link LogicalOpGPOptAdd}.
     */
    protected BaseForPhysicalOpSingleInputJoin( final UnaryLogicalOp lop ) {
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
