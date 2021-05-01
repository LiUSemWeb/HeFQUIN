package se.liu.ida.hefquin.queryplan.physical.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.query.jenaimpl.JenaBasedQueryPatternUtils;
import se.liu.ida.hefquin.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.queryplan.physical.UnaryPhysicalOpForLogicalOp;

public abstract class BasePhysicalOpSingleInputJoin implements UnaryPhysicalOpForLogicalOp
{
    protected final UnaryLogicalOp lop;

    /**
     * The given logical operator is expected to be of one of the following
     * two types: {@link LogicalOpTPAdd} or {@link LogicalOpBGPAdd}.
     */
    protected BasePhysicalOpSingleInputJoin( final UnaryLogicalOp lop ) {
        assert lop != null;
        assert (lop instanceof LogicalOpBGPAdd) || (lop instanceof LogicalOpTPAdd);
        this.lop = lop;
    }

    @Override
    public UnaryLogicalOp getLogicalOperator() { return lop; }

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		final Set<Var> certainVars = new HashSet<>( inputVars[0].getCertainVariables() );
		final Set<Var> possibleVars = new HashSet<>( inputVars[0].getPossibleVariables() );

		if ( lop instanceof LogicalOpTPAdd ) {
			final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) lop;
			certainVars.addAll( JenaBasedQueryPatternUtils.getVariablesInPattern(tpAdd.getTP()) );
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			final LogicalOpBGPAdd bgpAdd = (LogicalOpBGPAdd) lop;
			certainVars.addAll( JenaBasedQueryPatternUtils.getVariablesInPattern(bgpAdd.getBGP()) );
		}
		else
			throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

}
