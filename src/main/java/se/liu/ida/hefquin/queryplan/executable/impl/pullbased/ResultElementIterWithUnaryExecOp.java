package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ResultElementIterWithUnaryExecOp extends ResultElementIterBase
{
	protected final MyOpRunnerThread opRunnerThread;

	public ResultElementIterWithUnaryExecOp( final UnaryExecutableOp op,
	                                         final ResultBlockIterator inputIter,
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

	@Override
	protected OpRunnerThread getOpRunnerThread() {
		return opRunnerThread;
	}


	protected class MyOpRunnerThread extends OpRunnerThread
	{
		private final UnaryExecutableOp op;
		protected final ResultBlockIterator inputIter;

		public MyOpRunnerThread( final UnaryExecutableOp op,
		                         final ResultBlockIterator inputIter )
		{
			this.op = op;
			this.inputIter = inputIter;
		}

		@Override
		public UnaryExecutableOp getOp() {
			return op;
		}

		@Override
		public void run() {
			while ( inputIter.hasNext() ) {
				op.process( inputIter.next(), sink, execCxt );
			}
			op.concludeExecution(sink, execCxt);
			sink.close();
		}

	} // end of class OpRunnerThread

}
