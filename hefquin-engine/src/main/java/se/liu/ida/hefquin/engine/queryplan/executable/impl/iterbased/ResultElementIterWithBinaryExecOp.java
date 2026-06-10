package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

public class ResultElementIterWithBinaryExecOp extends ResultElementIterBase
{
	protected final MyOpRunnerThread opRunnerThread;

	public ResultElementIterWithBinaryExecOp( final BinaryExecutableOp op,
	                                          final ResultElementIterator inputIter1,
	                                          final ResultElementIterator inputIter2,
	                                          final QueryProcContextExt ctx )
	{
		super(ctx);

		assert op != null;
		assert inputIter1 != null;
		assert inputIter2 != null;

		opRunnerThread = new MyOpRunnerThread( op, inputIter1, inputIter2 );
	}

	@Override
	public BinaryExecutableOp getOp() {
		return opRunnerThread.getOp();
	}

	public ExecutablePlanStats tryGetStatsOfInput1() {
		return ResultIteratorUtils.tryGetStatsOfProducingSubPlan( opRunnerThread.getInput1() );
	}

	public ExecutablePlanStats tryGetStatsOfInput2() {
		return ResultIteratorUtils.tryGetStatsOfProducingSubPlan( opRunnerThread.getInput2() );
	}

	public List<Exception> tryGetExceptionsOfInput1() {
		return ResultIteratorUtils.tryGetExceptionsOfProducingSubPlan( opRunnerThread.getInput1() );
	}

	public List<Exception> tryGetExceptionsOfInput2() {
		return ResultIteratorUtils.tryGetExceptionsOfProducingSubPlan( opRunnerThread.getInput2() );
	}

	@Override
	protected OpRunnerThread getOpRunnerThread() {
		return opRunnerThread;
	}


	protected class MyOpRunnerThread extends OpRunnerThread
	{
		private final BinaryExecutableOp op;
		protected final ResultElementIterator inputIter1;
		protected final ResultElementIterator inputIter2;

		public MyOpRunnerThread( final BinaryExecutableOp op,
		                         final ResultElementIterator inputIter1,
		                         final ResultElementIterator inputIter2 )
		{
			this.op = op;
			this.inputIter1 = inputIter1;
			this.inputIter2 = inputIter2;
		}

		@Override
		public BinaryExecutableOp getOp() { return op; }

		public ResultElementIterator getInput1() { return inputIter1; }

		public ResultElementIterator getInput2() { return inputIter2; }

		@Override
		protected void _run() throws ExecutionException {
			// Note, we do not need to check op.requiresCompleteChild1InputFirst()
			// here because this implementation is anyways sending the complete
			// intermediate result from input one first, before moving on to
			// input two.

			while ( inputIter1.hasNext() ) {
				op.processInputFromChild1( inputIter1.next(), sink, ctx );
			}
			op.wrapUpForChild1(sink, ctx);

			while ( inputIter2.hasNext() ) {
				op.processInputFromChild2( inputIter2.next(), sink, ctx );
			}
			op.wrapUpForChild2(sink, ctx);
		}

	} // end of class OpRunnerThread

}
