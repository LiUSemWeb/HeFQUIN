package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpHashRJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRightJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

public class PhysicalOpHashRJoin implements BinaryPhysicalOpForLogicalOp
{
	protected final LogicalOpRightJoin lop;

	public PhysicalOpHashRJoin(final LogicalOpRightJoin lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public BinaryLogicalOp getLogicalOperator() {
		return lop;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

		final Set<Var> certainVars = inputVars[1].getCertainVariables();

		final Set<Var> possibleVars = new HashSet<>();
		possibleVars.addAll( inputVars[0].getCertainVariables() );
		possibleVars.addAll( inputVars[0].getPossibleVariables() );
		possibleVars.addAll( inputVars[1].getPossibleVariables() );
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public BinaryExecutableOp createExecOp( final ExpectedVariables ... inputVars ) {
		assert inputVars.length == 2;

		return new ExecOpHashRJoin( inputVars[0], inputVars[1] );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpHashRJoin && ((PhysicalOpHashRJoin) o).lop.equals(lop);
	}

	@Override
	public int hashCode(){
		return lop.hashCode() ^ Objects.hash( this.getClass().getName() );
	}

	@Override
	public String toString(){
		return "> hashRJoin ";
	}

}
