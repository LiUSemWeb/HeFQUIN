package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOpForLogicalOp;

import java.util.HashSet;
import java.util.Set;

public abstract class BasePhysicalOpBinaryJoin implements BinaryPhysicalOpForLogicalOp
{
	protected final LogicalOpJoin lop;

	protected BasePhysicalOpBinaryJoin(final LogicalOpJoin lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

		final Set<Var> certainVars = new HashSet<>( inputVars[0].getCertainVariables());
		final Set<Var> possibleVars = new HashSet<>( inputVars[0].getPossibleVariables() );

		certainVars.addAll( inputVars[1].getCertainVariables() );
		possibleVars.addAll(inputVars[1].getPossibleVariables());
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
