package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.queryplan.executable.impl.ops.BinaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ResultElementIterWithBinaryExecOp extends ResultElementIterBase
{
	protected final MyOpRunnerThread opRunnerThread;

	public ResultElementIterWithBinaryExecOp( final BinaryExecutableOp op,
	                                          final ResultBlockIterator inputIter1,
	                                          final ResultBlockIterator inputIter2,
	                                          final ExecutionContext execCxt )
	{
		super(execCxt);

		assert op != null;
		assert inputIter1 != null;
		assert inputIter2 != null;
		assert execCxt != null;

		opRunnerThread = new MyOpRunnerThread( op, inputIter1, inputIter2 );
	}

	@Override
	public BinaryExecutableOp getOp() {
		return opRunnerThread.getOp();
	}

	@Override
	protected OpRunnerThread getOpRunnerThread() {
		return opRunnerThread;
	}


	protected class MyOpRunnerThread extends OpRunnerThread
	{
		private final BinaryExecutableOp op;
		protected final ResultBlockIterator inputIter1;
		protected final ResultBlockIterator inputIter2;

		public MyOpRunnerThread( final BinaryExecutableOp op,
		                         final ResultBlockIterator inputIter1,
		                         final ResultBlockIterator inputIter2 )
		{
			this.op = op;
			this.inputIter1 = inputIter1;
			this.inputIter2 = inputIter2;
		}

		@Override
		public BinaryExecutableOp getOp() {
			return op;
		}

		@Override
		public void run() {
			while ( inputIter1.hasNext() ) {
				op.processBlockFromChild1( inputIter1.next(), sink, execCxt );
			}
			op.wrapUpForChild1(sink, execCxt);

			while ( inputIter2.hasNext() ) {
				op.processBlockFromChild2( inputIter2.next(), sink, execCxt );
			}
			op.wrapUpForChild2(sink, execCxt);

			sink.close();
		}

	} // end of class OpRunnerThread

}
