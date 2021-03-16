package se.liu.ida.hefquin.queryplan;

public interface PhysicalOperator
{
	/**
	 * Returns an {@link ExecutableOperatorCreator} that can create the
	 * {@link ExecutableOperator} to be used for this physical operator.
	 */
	ExecutableOperatorCreator<?> getExecOpCreator();
}
