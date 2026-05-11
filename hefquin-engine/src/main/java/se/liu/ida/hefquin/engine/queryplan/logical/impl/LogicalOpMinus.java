package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpMinus extends BaseForLogicalOps implements BinaryLogicalOp
{
	protected static final LogicalOpMinus singletonWithoutReduction = new LogicalOpMinus(false);
	protected static final LogicalOpMinus singletonThatMayReduce  = new LogicalOpMinus(true);

	public static LogicalOpMinus getInstance( final boolean mayReduce ) {
		return mayReduce ? singletonThatMayReduce : singletonWithoutReduction;
	}

	/**
	 * Returns the singleton instance of {@link LogicalOpMinus} that does <em>not</em>
	 * reduce duplicates.
	 *
	 * <p>This is equivalent to calling {@link #getInstance(boolean)} with the argument
	 * {@code false}.
	 *
	 * @return the singleton instance that does not reduce duplicates
	 */
	public static LogicalOpMinus getInstance() {
		return singletonWithoutReduction;
	}

	protected LogicalOpMinus( final boolean mayReduce ) {
		super( mayReduce );
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

		return inputVars[0];
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpMinus oo
		    && oo.mayReduce == mayReduce;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "minus";
	}

}