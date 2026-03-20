package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

/**
 * Implementations of this interface provide the functionality
 * to print logical plans in some way.
 */
public interface LogicalPlanPrinter
{
	public static enum LogicalPlanStage {
		SOURCE_ASSIGNMENT("Source Assignment"),
		FINAL_LOGICAL_PLAN("Final Logical Plan");

		public final String name;
		LogicalPlanStage( final String name ) { this.name = name; }
	}
	/**
	 * Prints the given plan to the given stream.
	 * Plan type is used to distinguish between different types of plans when printing.
	 */
	void print( LogicalPlan plan, PrintStream out, LogicalPlanStage planType );
	
	/**
	 * Prints the given plan to the stream(s) that are stored in this printer.
	 * Plan type is used to distinguish between different types of plans when printing.
	 */
	void print( LogicalPlan plan, LogicalPlanStage planType );
}
