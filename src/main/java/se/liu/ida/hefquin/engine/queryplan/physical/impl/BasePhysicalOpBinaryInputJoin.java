package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOpForLogicalOp;

import java.util.HashSet;
import java.util.Set;

public abstract class BasePhysicalOpBinaryInputJoin implements BinaryPhysicalOpForLogicalOp
{
	protected final BinaryLogicalOp lop;

	protected BasePhysicalOpBinaryInputJoin(BinaryLogicalOp lop) {
		assert lop != null;
		assert (lop instanceof LogicalOpJoin);
		this.lop = lop;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

		final Set<Var> certainVarsL = new HashSet<>( inputVars[0].getCertainVariables());
		final Set<Var> possibleVarsL = new HashSet<>( inputVars[0].getPossibleVariables() );

		final Set<Var> certainVarsR = new HashSet<>( inputVars[1].getCertainVariables());
		final Set<Var> possibleVarsR = new HashSet<>( inputVars[1].getPossibleVariables() );

		certainVarsL.addAll(certainVarsR);
		possibleVarsL.addAll(possibleVarsR);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVarsL;}
			@Override public Set<Var> getPossibleVariables() { return possibleVarsL;}
		};
	}

	@Override
	public BinaryLogicalOp getLogicalOperator() {
		return lop;
	}

}
