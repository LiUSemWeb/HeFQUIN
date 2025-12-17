package se.liu.ida.hefquin.rml;

public class RMLParserException extends Exception
{
	private static final long serialVersionUID = -7019128373862946801L;

	public RMLParserException( final String msg, final Throwable cause ) {
		super(msg, cause);
	}

	public RMLParserException( final String msg ) {
		super(msg);
	}
}
