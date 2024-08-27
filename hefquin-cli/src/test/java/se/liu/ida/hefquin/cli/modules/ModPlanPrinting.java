package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.TextBasedLogicalPlanPrinterImpl;
import se.liu.ida.hefquin.engine.queryplan.utils.TextBasedPhysicalPlanPrinterImpl;

public class ModPlanPrinting extends ModBase
{
	protected final ArgDecl argPrintSrcAssignment  = new ArgDecl(ArgDecl.NoValue, "printSourceAssignment");
	protected final ArgDecl argPrintLogicalPlan    = new ArgDecl(ArgDecl.NoValue, "printLogicalPlan");
	protected final ArgDecl argPrintPhysicalPlan   = new ArgDecl(ArgDecl.NoValue, "printPhysicalPlan");

	protected LogicalPlanPrinter srcasgPrinter = null;
	protected LogicalPlanPrinter lplanPrinter = null;
	protected PhysicalPlanPrinter pplanPrinter = null;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Query Plan Printing");
		cmdLine.add(argPrintSrcAssignment, "--printSourceAssignment", "Print out the source assignment used as input to the query optimization");
		cmdLine.add(argPrintLogicalPlan, "--printLogicalPlan", "Print out the logical plan produced by the logical query optimization");
		cmdLine.add(argPrintPhysicalPlan, "--printPhysicalPlan", "Print out the physical plan produced by the physical query optimization and used for the query execution");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		if ( cmdLine.contains(argPrintSrcAssignment) )
			srcasgPrinter = new TextBasedLogicalPlanPrinterImpl();

		if ( cmdLine.contains(argPrintLogicalPlan) )
			lplanPrinter = new TextBasedLogicalPlanPrinterImpl();

		if ( cmdLine.contains(argPrintPhysicalPlan) )
			pplanPrinter = new TextBasedPhysicalPlanPrinterImpl();
	}

	public LogicalPlanPrinter getSourceAssignmentPrinter() { return srcasgPrinter; }

	public LogicalPlanPrinter getLogicalPlanPrinter() { return lplanPrinter; }

	public PhysicalPlanPrinter getPhysicalPlanPrinter() { return pplanPrinter; }
}
