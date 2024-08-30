package se.liu.ida.hefquin.base.utils;

import java.io.PrintStream;

/**
 * Wraps a {@link PrintStream} and adds functionality to print with
 * different levels of indentation.
 */
public class IndentingPrintStream
{
	protected final PrintStream out;

	protected int indentLevel = 0;

	public IndentingPrintStream( final PrintStream out ) {
		assert out != null;

		this.out = out;
	}

	public void increaseIndentationLevel() {
		indentLevel++;
	}

	public void decreaseIndentationLevel() {
		indentLevel--;
	}

	public void appendIndentation() {
		for ( int i = 0; i < indentLevel; i++ )
			out.append("  ");
	}

	public void append( final String s ) {
		out.append(s);
	}

	public void flush() {
		out.flush();
	}

}
