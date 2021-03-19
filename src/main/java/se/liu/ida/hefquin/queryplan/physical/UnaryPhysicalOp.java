package se.liu.ida.hefquin.queryplan.physical;

import se.liu.ida.hefquin.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.UnaryExecutableOp;

public interface UnaryPhysicalOp extends PhysicalOperator
{
	/**
	 * Convenience method that always should return the same as what is returned
	 * by calling {@link PhysicalOperator#getChild(int)} with a value of 0.
	 */
	PhysicalOperator getChildOp();

	@Override
	UnaryExecutableOp createExecOp();
}
