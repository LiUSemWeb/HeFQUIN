package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;

public abstract class BaseForExecOps implements ExecutableOperator
{
	/**
	 * If <code>true</code>, then the subclasses are expected to collect exceptions (by
	 * calling {@link #recordExceptionCaughtDuringExecution(Exception)}); otherwise, they
	 * are expected to throw the exceptions immediately.
	 */
	protected final boolean collectExceptions;

	private List<Exception> exceptionsCaughtDuringExecution = null;

	public BaseForExecOps( final boolean collectExceptions ) {
		this.collectExceptions = collectExceptions;
	}

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
