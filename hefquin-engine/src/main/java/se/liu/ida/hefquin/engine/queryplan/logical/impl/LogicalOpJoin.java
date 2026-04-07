package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class LogicalOpJoin extends BaseForLogicalOps implements BinaryLogicalOp
{
	protected static final LogicalOpJoin singletonWithoutReduction = new LogicalOpJoin(false);
	protected static final LogicalOpJoin singletonThatMayReduce  = new LogicalOpJoin(true);

	public static LogicalOpJoin getInstance( final boolean mayReduce ) {
		return mayReduce ? singletonThatMayReduce : singletonWithoutReduction;
	}

	/**
	 * Returns the singleton instance of {@link LogicalOpJoin} that does <em>not</em>
	 * reduce duplicates.
	 *
	 * <p>This is equivalent to calling {@link #getInstance(boolean)} with the argument
	 * {@code false}.
	 *
	 * @return the singleton instance that does not reduce duplicates
	 */
	public static LogicalOpJoin getInstance() {
		return singletonWithoutReduction;
	}

	protected LogicalOpJoin( final boolean mayReduce ) {
		super( mayReduce );
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

		final Set<Var> certainVars = ExpectedVariablesUtils.unionOfCertainVariables(inputVars);
		final Set<Var> possibleVars = ExpectedVariablesUtils.unionOfPossibleVariables(inputVars);
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpJoin oo
		    && oo.mayReduce == mayReduce;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "join";
	}

}
