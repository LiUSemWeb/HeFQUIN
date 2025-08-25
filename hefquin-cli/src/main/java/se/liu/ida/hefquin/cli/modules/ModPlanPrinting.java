package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

import se.liu.ida.hefquin.engine.queryplan.utils.ExecutablePlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.TextBasedExecutablePlanPrinterImpl;
import se.liu.ida.hefquin.engine.queryplan.utils.TextBasedLogicalPlanPrinterImpl;
import se.liu.ida.hefquin.engine.queryplan.utils.TextBasedPhysicalPlanPrinterImpl;

public class ModPlanPrinting extends ModBase
{
	protected final ArgDecl argPrintSrcAssignment  = new ArgDecl(ArgDecl.NoValue, "printSourceAssignment");
	protected final ArgDecl argPrintLogicalPlan    = new ArgDecl(ArgDecl.NoValue, "printLogicalPlan");
	protected final ArgDecl argPrintPhysicalPlan   = new ArgDecl(ArgDecl.NoValue, "printPhysicalPlan");
	protected final ArgDecl argPrintExecutablePlan = new ArgDecl(ArgDecl.NoValue, "printExecutablePlan");

	protected LogicalPlanPrinter srcasgPrinter = null;
	protected LogicalPlanPrinter lplanPrinter = null;
	protected PhysicalPlanPrinter pplanPrinter = null;
	protected ExecutablePlanPrinter eplanPrinter = null;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Query Plan Printing");
		cmdLine.add(argPrintSrcAssignment, "--printSourceAssignment", "Print out the source assignment used as input to the query optimization");
		cmdLine.add(argPrintLogicalPlan, "--printLogicalPlan", "Print out the logical plan produced by the logical query optimization");
		cmdLine.add(argPrintPhysicalPlan, "--printPhysicalPlan", "Print out the physical plan produced by the physical query optimization and used for the query execution");
		cmdLine.add(argPrintExecutablePlan, "--printExecutablePlan", "Print out the executable plan");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		if ( cmdLine.contains(argPrintSrcAssignment) )
			srcasgPrinter = new TextBasedLogicalPlanPrinterImpl();

		if ( cmdLine.contains(argPrintLogicalPlan) )
			lplanPrinter = new TextBasedLogicalPlanPrinterImpl();

		if ( cmdLine.contains(argPrintPhysicalPlan) )
			pplanPrinter = new TextBasedPhysicalPlanPrinterImpl();

		if ( cmdLine.contains(argPrintExecutablePlan) )
			eplanPrinter = new TextBasedExecutablePlanPrinterImpl();
	}

	public LogicalPlanPrinter getSourceAssignmentPrinter() { return srcasgPrinter; }

	public LogicalPlanPrinter getLogicalPlanPrinter() { return lplanPrinter; }

	public PhysicalPlanPrinter getPhysicalPlanPrinter() { return pplanPrinter; }

	public ExecutablePlanPrinter getExecutablePlanPrinter() { return eplanPrinter; }
}
