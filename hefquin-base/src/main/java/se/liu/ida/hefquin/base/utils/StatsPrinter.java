package se.liu.ida.hefquin.base.utils;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

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
		if ( s == null ) {
			addTabs(out, indentLevel);
			out.append( "null" );
			out.append( System.lineSeparator() );
			return;
		}

		if ( s.isEmpty() ) {
			addTabs(out, indentLevel);
			out.append( "--empty--" );
			out.append( System.lineSeparator() );
			return;
		}

		for ( final String entryName : s.getEntryNames() ) {
			final Object entry = s.getEntry(entryName);

			addTabs(out, indentLevel);
			out.append(entryName + " : ");

			if ( entry == null ) {
				out.append( "null" );
			}
			else if ( entry instanceof Stats )
			{
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
			else if ( entry instanceof List<?> )
			{
				final List<?> list = (List<?>) entry;
				if ( list.isEmpty() ) {
					out.append( "[ ]" );
				}
				else if ( list.get(0) instanceof Stats ) {
					if ( ! recursive ) {
						out.append( "[ ... ]" );
					}
					else {
						out.append( "[" + System.lineSeparator() );
						for ( final Object o : list ) {
							addTabs(out, indentLevel+1);
							out.append( "{" + System.lineSeparator() );

							print( (Stats) o, out, recursive, indentLevel+2 );

							addTabs(out, indentLevel+1);
							out.append( "}" + System.lineSeparator() );
						}
						addTabs(out, indentLevel);
						out.append( "]" );
					}
				}
				else {
					out.append( list.toString() );
				}
			}
			else if ( entry.getClass().isArray() )
			{
				out.append( "[ " + ArrayUtils.toString(entry) + " ]" );
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
