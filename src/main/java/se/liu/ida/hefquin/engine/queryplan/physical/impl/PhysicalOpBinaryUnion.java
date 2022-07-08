package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBinaryUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;

public class PhysicalOpBinaryUnion implements BinaryPhysicalOpForLogicalOp
{
	protected final LogicalOpUnion lop;

	public PhysicalOpBinaryUnion( final LogicalOpUnion lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBinaryUnion && ((PhysicalOpBinaryUnion) o).lop.equals(lop);
	}

	@Override
	public int hashCode(){
		return lop.hashCode();
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

		final Set<Var> certainVars = ExpectedVariablesUtils.intersectionOfCertainVariables(inputVars);
		final Set<Var> possibleVars = ExpectedVariablesUtils.unionOfAllVariables(inputVars);
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars;}
			@Override public Set<Var> getPossibleVariables() { return possibleVars;}
		};
	}

	@Override
	public void visit(final PhysicalPlanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public BinaryExecutableOp createExecOp( final ExpectedVariables... inputVars ) {
		return new ExecOpBinaryUnion();
	}

	@Override
	public BinaryLogicalOp getLogicalOperator() {
		return lop;
	}

	@Override
	public String toString(){
		return "> binaryUnion ";
	}

}
