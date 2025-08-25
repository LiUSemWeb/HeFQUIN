package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;

/**
 * Implementations of this interface provide the functionality
 * to print executable plans in some way.
 */
public interface ExecutablePlanPrinter
{
	/**
	 * Prints the given plan to the given stream.
	 */
	void print( ExecutablePlan plan, PrintStream out );
}
