package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.queryplan.executable.impl.ClosableIntermediateResultElementSink;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.BinaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ResultElementIterWithBinaryExecOp extends ResultElementIterBase
{
	protected final OpRunnerThread opRunnerThread;

	public ResultElementIterWithBinaryExecOp( final BinaryExecutableOp op,
	                                          final ResultBlockIterator inputIter1,
	                                          final ResultBlockIterator inputIter2,
	                                          final ExecutionContext execCxt )
	{
		assert op != null;
		assert inputIter1 != null;
		assert inputIter2 != null;
		assert execCxt != null;

		opRunnerThread = new OpRunnerThread( op, inputIter1, inputIter2, sink, execCxt );
	}

	public BinaryExecutableOp getOp() {
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
		private final BinaryExecutableOp op;
		protected final ResultBlockIterator inputIter1;
		protected final ResultBlockIterator inputIter2;
		protected final ClosableIntermediateResultElementSink sink;
		protected final ExecutionContext execCxt;

		public OpRunnerThread( final BinaryExecutableOp op,
		                       final ResultBlockIterator inputIter1,
		                       final ResultBlockIterator inputIter2,
		                       final ClosableIntermediateResultElementSink sink,
		                       final ExecutionContext execCxt )
		{
			this.op = op;
			this.inputIter1 = inputIter1;
			this.inputIter2 = inputIter2;
			this.sink = sink;
			this.execCxt = execCxt;
		}

		public BinaryExecutableOp getOp() {
			return op;
		}

		public void run() {
			while ( inputIter1.hasNext() ) {
				op.processBlockFromChild1( inputIter1.next(), sink, execCxt );
			}

			while ( inputIter2.hasNext() ) {
				op.processBlockFromChild2( inputIter2.next(), sink, execCxt );
			}

			op.concludeExecution(sink, execCxt);
			sink.close();
		}

	} // end of class OpRunnerThread

}
