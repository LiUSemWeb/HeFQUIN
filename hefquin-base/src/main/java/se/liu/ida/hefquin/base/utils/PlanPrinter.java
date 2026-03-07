package se.liu.ida.hefquin.base.utils;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

public class PlanPrinter
{
	protected static final String propertyIndentWithSubPlans =    "|  - ";
	protected static final String propertyIndentWithoutSubPlans = "   - ";
	protected static final String subplanIndent = "   ";

	public static void print( final PrintablePlan p ) {
		print( p, System.out );
	}

	public static void print( final PrintablePlan p, final PrintStream out ) {
		print(p, out, "", "");
	}

	public static void print( final PrintablePlan p,
	                          final PrintStream out,
	                          final String indentFirst,
	                          final String indentRest ) {
		out.println( indentFirst + p.rootOpAsString );

		if ( p.rootOpProperties != null ) {
			final String indentProps;
			if ( p.subPlans != null && !p.subPlans.isEmpty() )
				indentProps = indentRest + "\u2502";  //  "|"
			else
				indentProps = indentRest + " ";

			for ( final String prop : p.rootOpProperties ) {
				out.println( indentProps + "  - " + prop );
			}
		}

		if ( p.subPlans != null ) {
			final Iterator<PrintablePlan> it = p.subPlans.iterator();
			while ( it.hasNext() ) {
				final PrintablePlan subPlan = it.next();

				final String newIndentFirst, newIndentRest;
				if ( it.hasNext() ) {
					newIndentFirst = indentRest + "├── ";
					newIndentRest = indentRest + "\u2502   ";
				}
				else {
					newIndentFirst = indentRest + "└── ";
					newIndentRest = indentRest + "    ";
				}

				out.println( indentRest + "\u2502" );
				print( subPlan, out, newIndentFirst, newIndentRest );
			}
		}
	}

	public static class PrintablePlan {
		public final String rootOpAsString;
		public final List<String> rootOpProperties;
		public final List<PrintablePlan> subPlans;

		public PrintablePlan( final String rootOpAsString,
		                      final List<String> rootOpProperties,
		                      final List<PrintablePlan> subPlans ) {
			assert rootOpAsString != null;
			this.rootOpAsString = rootOpAsString;
			this.rootOpProperties = rootOpProperties;
			this.subPlans = subPlans;
		}
	}
}
