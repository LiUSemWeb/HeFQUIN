package se.liu.ida.hefquin.engine.wrappers.lpg.impl.exceptions;

public class UnSupportedEdgeLabelException extends IllegalArgumentException
{
	private static final long serialVersionUID = -2059853316655602721L;

	public UnSupportedEdgeLabelException( final String message ) {
		super(message);
	}
}
