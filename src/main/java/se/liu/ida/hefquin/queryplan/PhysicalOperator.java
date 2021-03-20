package se.liu.ida.hefquin.queryplan;

public interface PhysicalOperator
{
	/**
	 * Creates and returns the executable operator to be used for
	 * this physical operator. The implementation of this method
	 * has to create a new {@link ExecutableOperator} object each
	 * time it is called.
	 */
	ExecutableOperator createExecOp();
}
