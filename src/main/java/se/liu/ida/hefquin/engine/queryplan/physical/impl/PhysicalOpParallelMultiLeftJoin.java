package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpParallelMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;

public class PhysicalOpParallelMultiLeftJoin implements UnaryPhysicalOp
{
	protected final List<LogicalOpRequest<?,?>> optionalParts;

	public PhysicalOpParallelMultiLeftJoin( final List<LogicalOpRequest<?,?>> optionalParts ) {
		assert ! optionalParts.isEmpty();
		this.optionalParts = optionalParts;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		final Set<Var> certainVars = inputVars[0].getCertainVariables();
		final Set<Var> possibleVars = inputVars[0].getPossibleVariables();

		for ( final LogicalOpRequest<?,?> req : optionalParts ) {
			final ExpectedVariables ev = req.getRequest().getExpectedVariables();
			possibleVars.addAll( ev.getCertainVariables() );
			possibleVars.addAll( ev.getPossibleVariables() );
		}

		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public UnaryExecutableOp createExecOp( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		return new ExecOpParallelMultiwayLeftJoin( optionalParts, inputVars[0] );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpParallelMultiLeftJoin
				&& ((PhysicalOpParallelMultiLeftJoin) o).optionalParts.equals(optionalParts);
	}

	@Override
	public int hashCode(){
		return optionalParts.hashCode();
	}

	@Override
	public String toString(){
		return "> parallelMultiLeftJoin with " + optionalParts.size() + " optional parts";
	}

}
