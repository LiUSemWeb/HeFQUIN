package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ResultElementIterWithUnaryExecOp extends ResultElementIterBase
{
	protected final MyOpRunnerThread opRunnerThread;

	public ResultElementIterWithUnaryExecOp( final UnaryExecutableOp op,
	                                         final ResultElementIterator inputIter,
	                                         final ExecutionContext execCxt )
	{
		super(execCxt);

		assert op != null;
		assert inputIter != null;

		opRunnerThread = new MyOpRunnerThread(op, inputIter);
	}

	@Override
	public UnaryExecutableOp getOp() {
		return opRunnerThread.getOp();
	}

	public ExecutablePlanStats tryGetStatsOfInput() {
		return ResultIteratorUtils.tryGetStatsOfProducingSubPlan( opRunnerThread.getInput() );
	}

	public List<Exception> tryGetExceptionsOfInput() {
		return ResultIteratorUtils.tryGetExceptionsOfProducingSubPlan( opRunnerThread.getInput() );
	}

	@Override
	protected OpRunnerThread getOpRunnerThread() {
		return opRunnerThread;
	}


	protected class MyOpRunnerThread extends OpRunnerThread
	{
		private final UnaryExecutableOp op;
		protected final ResultElementIterator inputIter;

		public MyOpRunnerThread( final UnaryExecutableOp op,
		                         final ResultElementIterator inputIter )
		{
			this.op = op;
			this.inputIter = inputIter;
		}

		@Override
		public UnaryExecutableOp getOp() {
			return op;
		}

		public ResultElementIterator getInput() {
			return inputIter;
		}

		@Override
		protected void _run() throws ExecutionException {
			while ( inputIter.hasNext() ) {
				op.process( inputIter.next(), sink, execCxt );
			}
			op.concludeExecution(sink, execCxt);
		}

	} // end of class OpRunnerThread

}
