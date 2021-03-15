package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.queryplan.executable.impl.ClosableIntermediateResultElementSink;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.BinaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ResultElementIterWithBinaryExecOp<InElmtType1,InElmtType2,OutElmtType> extends ResultElementIterBase<OutElmtType>
{
	protected final OpRunnerThread<InElmtType1,InElmtType2,OutElmtType> opRunnerThread;

	public ResultElementIterWithBinaryExecOp( final BinaryExecutableOp<InElmtType1,InElmtType2,OutElmtType> op,
	                                          final ResultBlockIterator<InElmtType1> inputIter1,
	                                          final ResultBlockIterator<InElmtType2> inputIter2,
	                                          final ExecutionContext execCxt )
	{
		assert op != null;
		assert inputIter1 != null;
		assert inputIter2 != null;
		assert execCxt != null;

		opRunnerThread = new OpRunnerThread<InElmtType1,InElmtType2,OutElmtType>( op, inputIter1, inputIter2, sink, execCxt );
	}

	public BinaryExecutableOp<InElmtType1,InElmtType2,OutElmtType> getOp() {
		return opRunnerThread.getOp();
	}

	@Override
	public void ensureOpRunnerThreadIsStarted() {
		if ( opRunnerThread.getState() == Thread.State.NEW ) {
			opRunnerThread.start();
		}
	}


	protected static class OpRunnerThread<InElmtType1,InElmtType2,OutElmtType> extends Thread
	{
		private final BinaryExecutableOp<InElmtType1,InElmtType2,OutElmtType> op;
		protected final ResultBlockIterator<InElmtType1> inputIter1;
		protected final ResultBlockIterator<InElmtType2> inputIter2;
		protected final ClosableIntermediateResultElementSink<OutElmtType> sink;
		protected final ExecutionContext execCxt;

		public OpRunnerThread( final BinaryExecutableOp<InElmtType1,InElmtType2,OutElmtType> op,
		                       final ResultBlockIterator<InElmtType1> inputIter1,
		                       final ResultBlockIterator<InElmtType2> inputIter2,
		                       final ClosableIntermediateResultElementSink<OutElmtType> sink,
		                       final ExecutionContext execCxt )
		{
			this.op = op;
			this.inputIter1 = inputIter1;
			this.inputIter2 = inputIter2;
			this.sink = sink;
			this.execCxt = execCxt;
		}

		public BinaryExecutableOp<InElmtType1,InElmtType2,OutElmtType> getOp() {
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
