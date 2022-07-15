package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecPlanTaskInterruptionException extends ExecutionException
{
	private static final long serialVersionUID = -8330416567179466442L;

	public ExecPlanTaskInterruptionException( final InterruptedException e ) {
		super(e);
	}

}
