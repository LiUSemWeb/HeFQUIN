package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.engine.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutablePlanStatsImpl;

public class ResultIteratorUtils
{
	public static ExecutablePlanStats tryGetStatsOfProducingSubPlan( final ResultBlockIterator it ) {
		if ( it instanceof ResultBlockIterOverResultElementIter ) {
			final ResultElementIterator eIt = ((ResultBlockIterOverResultElementIter) it).getElementIterator();
			return tryGetStatsOfProducingSubPlan(eIt);
		}

		return null;
	}

	public static ExecutablePlanStats tryGetStatsOfProducingSubPlan( final ResultElementIterator it ) {
		final ExecutableOperatorStats rootOpStats = ResultIteratorUtils.tryGetStatsOfProducingOperator(it);
		if ( it instanceof ResultElementIterWithNullaryExecOp ) {
			return new ExecutablePlanStatsImpl( rootOpStats );
		}
		else if ( it instanceof ResultElementIterWithUnaryExecOp ) {
			final ResultElementIterWithUnaryExecOp itt = (ResultElementIterWithUnaryExecOp) it;
			return new ExecutablePlanStatsImpl( rootOpStats, itt.tryGetStatsOfInput() );
		}
		else if ( it instanceof ResultElementIterWithBinaryExecOp ) {
			final ResultElementIterWithBinaryExecOp itt = (ResultElementIterWithBinaryExecOp) it;
			return new ExecutablePlanStatsImpl( rootOpStats, itt.tryGetStatsOfInput1(), itt.tryGetStatsOfInput2() );
		}

		return new ExecutablePlanStatsImpl( rootOpStats );
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

}
