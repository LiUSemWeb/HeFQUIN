package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;

public abstract class BaseForExecOps implements ExecutableOperator
{
	private List<Exception> exceptionsCaughtDuringExecution = null;

	@Override
	public List<Exception> getExceptionsCaughtDuringExecution() {
		if ( exceptionsCaughtDuringExecution == null )
			return Collections.emptyList();
		else
			return exceptionsCaughtDuringExecution;
	}

	protected void recordExceptionCaughtDuringExecution( final Exception e ) {
		assert e != null;

		if ( exceptionsCaughtDuringExecution == null ) {
			exceptionsCaughtDuringExecution = new ArrayList<>();
		}

		exceptionsCaughtDuringExecution.add(e);
	}
}
