package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ResultElementIterException extends RuntimeException
{
	private static final long serialVersionUID = 8757864241387777953L;

	protected final ExecutionException execExptn;

	public ResultElementIterException( final ExecutionException execExptn ) {
		assert execExptn != null;
		this.execExptn= execExptn;
	}

	public ExecutionException getWrappedExecutionException() {
		return execExptn;
	}
}
