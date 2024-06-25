package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

public class ModPlanPrinting extends ModBase
{
	protected final ArgDecl argPrintSrcAssignment  = new ArgDecl(ArgDecl.NoValue, "printSourceAssignment");
	protected final ArgDecl argPrintLogicalPlan    = new ArgDecl(ArgDecl.NoValue, "printLogicalPlan");
	protected final ArgDecl argPrintPhysicalPlan   = new ArgDecl(ArgDecl.NoValue, "printPhysicalPlan");

	protected boolean printSrcAssignment = false;
	protected boolean printLogicalPlan = false;
	protected boolean printPhysicalPlan = false;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Query Plan Printing");
		cmdLine.add(argPrintSrcAssignment, "--printSourceAssignment", "Print out the source assignment used as input to the query optimization");
		cmdLine.add(argPrintLogicalPlan, "--printLogicalPlan", "Print out the logical plan produced by the logical query optimization");
		cmdLine.add(argPrintPhysicalPlan, "--printPhysicalPlan", "Print out the physical plan produced by the physical query optimization and used for the query execution");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		printSrcAssignment = cmdLine.contains(argPrintSrcAssignment);
		printLogicalPlan = cmdLine.contains(argPrintLogicalPlan);
		printPhysicalPlan = cmdLine.contains(argPrintPhysicalPlan);
	}

	public boolean printSrcAssignment() { return printSrcAssignment; }

	public boolean printLogicalPlan() { return printLogicalPlan; }

	public boolean printPhysicalPlan() { return printPhysicalPlan; }
}
