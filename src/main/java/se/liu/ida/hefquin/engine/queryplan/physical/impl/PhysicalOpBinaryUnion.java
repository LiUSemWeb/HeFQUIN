package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBinaryUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOpForLogicalOp;

public class PhysicalOpBinaryUnion implements BinaryPhysicalOpForLogicalOp{
	
	protected final LogicalOpUnion lop;

	protected PhysicalOpBinaryUnion( final LogicalOpUnion lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {

		// perhaps this should be offered as the default implementation in the interface.
		
		assert inputVars.length == 2;

		final Set<Var> certainVars = new HashSet<>( inputVars[0].getCertainVariables());
		final Set<Var> possibleVars = new HashSet<>( inputVars[0].getPossibleVariables() );

		certainVars.addAll(inputVars[1].getCertainVariables());
		possibleVars.addAll(inputVars[1].getPossibleVariables());
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars;}
			@Override public Set<Var> getPossibleVariables() { return possibleVars;}
		};
	}

	@Override
	public BinaryExecutableOp createExecOp(ExpectedVariables... inputVars) {
		return new ExecOpBinaryUnion();
	}

	@Override
	public BinaryLogicalOp getLogicalOperator() {
		return lop;
	}

}
