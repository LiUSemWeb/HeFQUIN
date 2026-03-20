package se.liu.ida.hefquin.cli.modules;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

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
	protected final ArgDecl argPrintSrcAssignmentToFile  = new ArgDecl(ArgDecl.HasValue, "printSourceAssignmentToFile");
	protected final ArgDecl argPrintLogicalPlanToFile    = new ArgDecl(ArgDecl.HasValue, "printLogicalPlanToFile");
	protected final ArgDecl argPrintPhysicalPlanToFile   = new ArgDecl(ArgDecl.HasValue, "printPhysicalPlanToFile");
	protected final ArgDecl argPrintExecutablePlanToFile = new ArgDecl(ArgDecl.HasValue, "printExecutablePlanToFile");

	protected LogicalPlanPrinter srcasgPrinter = null;
	protected LogicalPlanPrinter lplanPrinter = null;
	protected PhysicalPlanPrinter pplanPrinter = null;
	protected ExecutablePlanPrinter eplanPrinter = null;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Query Plan Printing");
		cmdLine.add(argPrintSrcAssignment, "--printSourceAssignment", "Print out the source assignment used as input to the query optimization");
		cmdLine.add(argPrintSrcAssignmentToFile, "--printSourceAssignmentToFile", "Print out the source assignment used as input to the query optimization to a file");
		cmdLine.add(argPrintLogicalPlan, "--printLogicalPlan", "Print out the logical plan produced by the logical query optimization");
		cmdLine.add(argPrintLogicalPlanToFile, "--printLogicalPlanToFile", "Print out the logical plan produced by the logical query optimization to a file");
		cmdLine.add(argPrintPhysicalPlan, "--printPhysicalPlan", "Print out the physical plan produced by the physical query optimization and used for the query execution");
		cmdLine.add(argPrintPhysicalPlanToFile, "--printPhysicalPlanToFile", "Print out the physical plan produced by the physical query optimization to a file");
		cmdLine.add(argPrintExecutablePlan, "--printExecutablePlan", "Print out the executable plan");
		cmdLine.add(argPrintExecutablePlanToFile, "--printExecutablePlanToFile", "Print out the executable plan to a file");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {	
		List<PrintStream> outsList = processPrintFlags( cmdLine, argPrintSrcAssignment, argPrintSrcAssignmentToFile );
		if ( ! outsList.isEmpty() )
			srcasgPrinter = new TextBasedLogicalPlanPrinterImpl( outsList.toArray(new PrintStream[0]) );

		outsList = processPrintFlags( cmdLine, argPrintLogicalPlan, argPrintLogicalPlanToFile );
		if ( ! outsList.isEmpty() ) 
			lplanPrinter = new TextBasedLogicalPlanPrinterImpl( outsList.toArray(new PrintStream[0]) );

		outsList = processPrintFlags( cmdLine, argPrintPhysicalPlan, argPrintPhysicalPlanToFile );
		if ( ! outsList.isEmpty() ) 
			pplanPrinter = new TextBasedPhysicalPlanPrinterImpl( outsList.toArray(new PrintStream[0]) );

		outsList = processPrintFlags( cmdLine, argPrintExecutablePlan, argPrintExecutablePlanToFile );
		if ( ! outsList.isEmpty() ) 
			eplanPrinter = new TextBasedExecutablePlanPrinterImpl( outsList.toArray(new PrintStream[0]) );
	}

	protected List<PrintStream> processPrintFlags( final CmdArgModule cmdLine, final ArgDecl argPrintPlanToTerminal, final ArgDecl argPrintPlanToFile ) {
		final List<PrintStream> outsList = new ArrayList<>();
		if ( cmdLine.contains(argPrintPlanToTerminal) ) {
			outsList.add( System.out );
		}
		if ( cmdLine.contains(argPrintPlanToFile) ) {
			try {
				final PrintStream fileOut = new PrintStream( new FileOutputStream(cmdLine.getValue(argPrintPlanToFile), true) );
				outsList.add( fileOut );
			} catch ( final FileNotFoundException e ) {
				cmdLine.cmdError( "Failed to create print stream for output destination: " + cmdLine.getValue(argPrintPlanToFile), false );
			}
		}
		return outsList;
	}

	public LogicalPlanPrinter getSourceAssignmentPrinter() { return srcasgPrinter; }

	public LogicalPlanPrinter getLogicalPlanPrinter() { return lplanPrinter; }

	public PhysicalPlanPrinter getPhysicalPlanPrinter() { return pplanPrinter; }

	public ExecutablePlanPrinter getExecutablePlanPrinter() { return eplanPrinter; }
}
