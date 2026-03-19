package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

/**
 * Implementations of this interface provide the functionality
 * to print physical plans in some way.
 */
public interface PhysicalPlanPrinter
{
	default String getFileOutputPath() { return null; }

	default boolean isPrintPlanToTerminal() { return true; }

	/**
	 * Prints the given plan to the given stream.
	 */
	void print( PhysicalPlan plan, PrintStream out );
}
