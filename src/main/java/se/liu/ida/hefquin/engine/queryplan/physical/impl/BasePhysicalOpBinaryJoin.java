package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;

import java.util.Objects;
import java.util.Set;

public abstract class BasePhysicalOpBinaryJoin implements BinaryPhysicalOpForLogicalOp
{
	protected final LogicalOpJoin lop;

	protected BasePhysicalOpBinaryJoin(final LogicalOpJoin lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof BinaryPhysicalOpForLogicalOp
				&& ((BinaryPhysicalOpForLogicalOp) o).getLogicalOperator().equals(lop);
	}

	@Override
	public int hashCode(){
		return lop.hashCode() ^ Objects.hash( this.getClass().getName() );
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

		final Set<Var> certainVars = ExpectedVariablesUtils.unionOfCertainVariables(inputVars);
		final Set<Var> possibleVars = ExpectedVariablesUtils.unionOfPossibleVariables(inputVars);
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars;}
			@Override public Set<Var> getPossibleVariables() { return possibleVars;}
		};
	}

	@Override
	public BinaryLogicalOp getLogicalOperator() {
		return lop;
	}

}
