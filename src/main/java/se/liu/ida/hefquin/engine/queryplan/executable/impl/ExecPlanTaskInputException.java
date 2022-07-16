package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecPlanTaskInputException extends ExecutionException
{
	private static final long serialVersionUID = -5963584792306532321L;

	public ExecPlanTaskInputException( final String message,
	                                   final Throwable cause ) {
		super(message, cause);
	}

	public ExecPlanTaskInputException( final String message ) {
		super(message);
	}

}
