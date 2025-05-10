package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;

public class ResultIteratorUtils
{
	public static ExecutablePlanStats tryGetStatsOfProducingSubPlan( final ResultElementIterator it ) {
		final ExecutableOperatorStats rootOpStats = ResultIteratorUtils.tryGetStatsOfProducingOperator(it);
		if ( it instanceof ResultElementIterWithNullaryExecOp ) {
			return new ExecutablePlanStatsOfIteratorBasedPlan( rootOpStats );
		}
		else if ( it instanceof ResultElementIterWithUnaryExecOp ) {
			final ResultElementIterWithUnaryExecOp itt = (ResultElementIterWithUnaryExecOp) it;
			return new ExecutablePlanStatsOfIteratorBasedPlan( rootOpStats, itt.tryGetStatsOfInput() );
		}
		else if ( it instanceof ResultElementIterWithBinaryExecOp ) {
			final ResultElementIterWithBinaryExecOp itt = (ResultElementIterWithBinaryExecOp) it;
			return new ExecutablePlanStatsOfIteratorBasedPlan( rootOpStats, itt.tryGetStatsOfInput1(), itt.tryGetStatsOfInput2() );
		}

		return new ExecutablePlanStatsOfIteratorBasedPlan( rootOpStats );
	}

	public static ExecutableOperatorStats tryGetStatsOfProducingOperator( final ResultElementIterator it ) {
		final ExecutableOperator op = tryGetProducingOperator(it);
		return ( op != null ) ? op.getStats() : null;
	}

	public static ExecutableOperator tryGetProducingOperator( final ResultElementIterator it ) {
		if ( it instanceof ResultElementIterBase ) {
			return ( (ResultElementIterBase) it ).getOp();
		}

		return null;
	}

	public static List<Exception> tryGetExceptionsOfProducingSubPlan( final ResultElementIterator it ) {
		final List<Exception> rootOpExceptions = ResultIteratorUtils.tryGetExceptionsOfProducingOperator(it);
		if ( it instanceof ResultElementIterWithNullaryExecOp ) {
			return rootOpExceptions;
		}
		else if ( it instanceof ResultElementIterWithUnaryExecOp ) {
			final ResultElementIterWithUnaryExecOp itt = (ResultElementIterWithUnaryExecOp) it;
			return mergeLists( rootOpExceptions, itt.tryGetExceptionsOfInput() );
		}
		else if ( it instanceof ResultElementIterWithBinaryExecOp ) {
			final ResultElementIterWithBinaryExecOp itt = (ResultElementIterWithBinaryExecOp) it;
			return mergeLists( rootOpExceptions, itt.tryGetExceptionsOfInput1(), itt.tryGetExceptionsOfInput2() );
		}

		return rootOpExceptions;
	}

	public static List<Exception> tryGetExceptionsOfProducingOperator( final ResultElementIterator it ) {
		final ExecutableOperator op = tryGetProducingOperator(it);
		return ( op != null ) ? op.getExceptionsCaughtDuringExecution() : null;
	}

	@SafeVarargs
	protected static List<Exception> mergeLists( final List<Exception> ... lists ) {
		int i = 0;
		List<Exception> firstNonNull = null;
		while ( i < lists.length && firstNonNull == null ) {
			firstNonNull = lists[i];
			i++;
		}

		if ( i == lists.length ) {
			return firstNonNull;
		}

		final List<Exception> result = new ArrayList<>(firstNonNull);
		while ( i < lists.length ) {
			if ( lists[i] != null ) {
				result.addAll( lists[i] );
			}
			i++;
		}

		return result;
	}

}
