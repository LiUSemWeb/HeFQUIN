package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;

public class TextBasedExecutablePlanPrinterImpl extends BaseForTextBasedPlanPrinters
		implements ExecutablePlanPrinter
{	
	@Override
	public void print( final ExecutablePlan plan, final PrintStream out ) {
		out.print( "Not implemented" );
	}
}
