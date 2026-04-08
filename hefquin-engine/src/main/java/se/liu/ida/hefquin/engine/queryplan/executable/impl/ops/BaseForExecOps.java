package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;

/**
 * Top-level base class for all implementations of {@link ExecutableOperator}.
 *
 * This base class handles the collection of exceptions that may occur during
 * the execution of the algorithm implemented by an executable operator, and
 * it stores the {@link QueryPlanningInfo} (if any).
 */
public abstract class BaseForExecOps implements ExecutableOperator
{
	/**
	 * May be {@code null}.
	 */
	protected final QueryPlanningInfo qpInfo;

	/**
	 * If {@code true}, then the subclasses are expected to collect exceptions
	 * (by calling {@link #recordExceptionCaughtDuringExecution(Exception)});
	 * otherwise, they are expected to throw the exceptions immediately.
	 */
	protected final boolean collectExceptions;

	/**
	 * If {@code true}, then the executable operator is permitted to reduce
	 * duplicate solution mappings as part of its execution; otherwise,
	 * the operator must preserve duplicates.
	 */
	protected final boolean mayReduce;

	private List<Exception> exceptionsCaughtDuringExecution = null;

	/**
	 * @param collectExceptions - if {@code true}, then the subclasses are
	 *           expected to collect exceptions (by calling
	 *           {@link #recordExceptionCaughtDuringExecution(Exception)});
	 *           otherwise, they are expected to throw the exceptions
	 *           immediately
	 * @param qpInfo - may be {@code null}
	 */
	public BaseForExecOps( final boolean collectExceptions,
	                       final QueryPlanningInfo qpInfo,
	                       final boolean mayReduce ) {
		this.collectExceptions = collectExceptions;
		this.qpInfo = qpInfo;
		this.mayReduce = mayReduce;
	}

	@Override
	public QueryPlanningInfo getQueryPlanningInfo() {
		return qpInfo;
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
