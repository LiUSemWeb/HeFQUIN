package se.liu.ida.hefquin.engine.queryplan;

public interface ExecutableOperator
{
	/**
	 * Returns the preferred block size of input blocks
	 * that are passed to this executable operator.
	 *
	 * A query planner may use this number as an optimization
	 * hint but it does not have to use it.
	 */
	int preferredInputBlockSize();
}
