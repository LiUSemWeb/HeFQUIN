package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.queryplan.executable.impl.ClosableIntermediateResultElementSink;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ResultElementIterWithUnaryExecOp<InElmtType,OutElmtType> extends ResultElementIterBase<OutElmtType>
{
	protected final OpRunnerThread<InElmtType,OutElmtType> opRunnerThread;

	public ResultElementIterWithUnaryExecOp( final UnaryExecutableOp<InElmtType,OutElmtType> op,
	                                         final ResultBlockIterator<InElmtType> inputIter,
	                                         final ExecutionContext execCxt )
	{
		assert op != null;
		assert inputIter != null;
		assert execCxt != null;

		opRunnerThread = new OpRunnerThread<InElmtType,OutElmtType>( op, inputIter, sink, execCxt );
	}

	public UnaryExecutableOp<InElmtType,OutElmtType> getOp() {
		return opRunnerThread.getOp();
	}

	@Override
	public void ensureOpRunnerThreadIsStarted() {
		if ( opRunnerThread.getState() == Thread.State.NEW ) {
			opRunnerThread.start();
		}
	}


	protected static class OpRunnerThread<InElmtType,OutElmtType> extends Thread
	{
		private final UnaryExecutableOp<InElmtType,OutElmtType> op;
		protected final ResultBlockIterator<InElmtType> inputIter;
		protected final ClosableIntermediateResultElementSink<OutElmtType> sink;
		protected final ExecutionContext execCxt;

		public OpRunnerThread( final UnaryExecutableOp<InElmtType,OutElmtType> op,
		                       final ResultBlockIterator<InElmtType> inputIter,
		                       final ClosableIntermediateResultElementSink<OutElmtType> sink,
		                       final ExecutionContext execCxt )
		{
			this.op = op;
			this.inputIter = inputIter;
			this.sink = sink;
			this.execCxt = execCxt;
		}

		public UnaryExecutableOp<InElmtType,OutElmtType> getOp() {
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
