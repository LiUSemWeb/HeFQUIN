package se.liu.ida.hefquin.engine.queryplan.executable;

import se.liu.ida.hefquin.base.utils.Stats;

public interface ExecutableOperatorStats extends Stats
{
	/**
	 * Returns the Java class name of the operator. This name should
	 * be the name of the actual class and not for some base class.
	 */
	String getClassName();

	/**
	 * Returns a short string representing the operator. Typically,
	 * this string would be the result of the toString() method of
	 * the operator.
	 */
	String getShortString();
}
