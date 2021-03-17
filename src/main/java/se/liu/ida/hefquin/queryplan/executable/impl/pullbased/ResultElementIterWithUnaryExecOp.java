package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.queryplan.executable.impl.ClosableIntermediateResultElementSink;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ResultElementIterWithUnaryExecOp extends ResultElementIterBase
{
	protected final OpRunnerThread opRunnerThread;

	public ResultElementIterWithUnaryExecOp( final UnaryExecutableOp op,
	                                         final ResultBlockIterator inputIter,
	                                         final ExecutionContext execCxt )
	{
		assert op != null;
		assert inputIter != null;
		assert execCxt != null;

		opRunnerThread = new OpRunnerThread( op, inputIter, sink, execCxt );
	}

	public UnaryExecutableOp getOp() {
		return opRunnerThread.getOp();
	}

	@Override
	public void ensureOpRunnerThreadIsStarted() {
		if ( opRunnerThread.getState() == Thread.State.NEW ) {
			opRunnerThread.start();
		}
	}


	protected static class OpRunnerThread extends Thread
	{
		private final UnaryExecutableOp op;
		protected final ResultBlockIterator inputIter;
		protected final ClosableIntermediateResultElementSink sink;
		protected final ExecutionContext execCxt;

		public OpRunnerThread( final UnaryExecutableOp op,
		                       final ResultBlockIterator inputIter,
		                       final ClosableIntermediateResultElementSink sink,
		                       final ExecutionContext execCxt )
		{
			this.op = op;
			this.inputIter = inputIter;
			this.sink = sink;
			this.execCxt = execCxt;
		}

		public UnaryExecutableOp getOp() {
			return op;
		}

		public void run() {
			while ( inputIter.hasNext() ) {
				op.process( inputIter.next(), sink, execCxt );
			}
			op.concludeExecution(sink, execCxt);
			sink.close();
		}

	} // end of class OpRunnerThread
}
