package se.liu.ida.hefquin.engine.utils;

import java.io.PrintStream;

public class StatsPrinter
{
	/**
	 * Prints the given stats to the given print stream.
	 * 
	 * Every entry that is a {@link Stats} object itself is
	 * printed as well if the 'recursive' flag is 'true'.
	 */
	public static void print( final Stats s, final PrintStream out, final boolean recursive ) {
		print(s, out, recursive, 0);
	}

	/**
	 * Prints the given stats to the given print stream at the given indentation level.
	 * 
	 * Every entry that is a {@link Stats} object itself is
	 * printed as well if the 'recursive' flag is 'true'.
	 */
	public static void print( final Stats s, final PrintStream out, final boolean recursive, final int indentLevel ) {
		for ( final String entryName : s.getEntryNames() ) {
			final Object entry = s.getEntry(entryName);

			addTabs(out, indentLevel);
			out.append(entryName + " : ");

			if ( entry instanceof Stats ) {
				final Stats ss = (Stats) entry;
				if ( ss.isEmpty() ) {
					out.append( "{ }" );
				}
				else if ( ! recursive ) {
					out.append( "{ ... }" );
				}
				else {
					out.append( "{" + System.lineSeparator() );
					print(ss, out, recursive, indentLevel+1);
					addTabs(out, indentLevel);
					out.append( "}" );
				}
			}
			else if ( entry == null) {
				out.append( "null" );
			}
			else {
				out.append( entry.toString() );
			}
			out.append( System.lineSeparator() );	
		}
	}


    protected static void addTabs( final PrintStream out, final int indentLevel ) {
        for (int i = 0; i < indentLevel; i++)
            out.append("  ");
    }

}
