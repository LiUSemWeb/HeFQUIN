package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Objects;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
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
		assert inputVars.length == 1;

		final SPARQLGraphPattern p;

		if ( lop instanceof LogicalOpTPAdd ) {
			p = ( (LogicalOpTPAdd) lop ).getTP();
		}
		else if ( lop instanceof LogicalOpTPOptAdd ) {
			p = ( (LogicalOpTPOptAdd) lop ).getTP();
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			p = ( (LogicalOpBGPAdd) lop ).getBGP();
		}
		else if ( lop instanceof LogicalOpBGPOptAdd ) {
			p = ( (LogicalOpBGPOptAdd) lop ).getBGP();
		}
		else if ( lop instanceof LogicalOpGPAdd ) {
			p = ( (LogicalOpGPAdd) lop ).getPattern();
		}
		else if ( lop instanceof LogicalOpGPOptAdd ) {
			p = ( (LogicalOpGPOptAdd) lop ).getPattern();
		}
		else
			throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );

		return QueryPatternUtils.getExpectedVariablesInPattern(p);
	}

}
