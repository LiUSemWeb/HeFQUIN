package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

/**
 * Implementations of this interface provide the functionality
 * to print logical plans in some way.
 */
public interface LogicalPlanPrinter
{
	/**
	 * Prints the given plan to the given stream.
	 */
	void print( LogicalPlan plan, PrintStream out, String planType );
	
	/**
	 * Prints the given plan to the stream(s) that are stored in this printer.
	 * Plan type is used to distinguish between different types of plans when printing.
	 */
	void print( LogicalPlan plan, String planType );
}
