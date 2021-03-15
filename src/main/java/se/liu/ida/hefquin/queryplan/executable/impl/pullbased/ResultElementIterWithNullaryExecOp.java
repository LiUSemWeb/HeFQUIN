package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.queryplan.executable.impl.ClosableIntermediateResultElementSink;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ResultElementIterWithNullaryExecOp<OutElmtType> extends ResultElementIterBase<OutElmtType>
{
	protected final OpRunnerThread<OutElmtType> opRunnerThread;

	public ResultElementIterWithNullaryExecOp( final NullaryExecutableOp<OutElmtType> op,
	                                           final ExecutionContext execCxt )
	{
		assert op != null;
		assert execCxt != null;

		opRunnerThread = new OpRunnerThread<OutElmtType>( op, sink, execCxt );
	}

	public NullaryExecutableOp<OutElmtType> getOp() {
		return opRunnerThread.getOp();
	}

	@Override
	public void ensureOpRunnerThreadIsStarted() {
		if ( opRunnerThread.getState() == Thread.State.NEW ) {
			opRunnerThread.start();
		}
	}


	protected static class OpRunnerThread<OutElmtType> extends Thread
	{
		private final NullaryExecutableOp<OutElmtType> op;
		protected final ClosableIntermediateResultElementSink<OutElmtType> sink;
		protected final ExecutionContext execCxt;

		public OpRunnerThread( final NullaryExecutableOp<OutElmtType> op,
		                       final ClosableIntermediateResultElementSink<OutElmtType> sink,
		                       final ExecutionContext execCxt )
		{
			this.op = op;
			this.sink = sink;
			this.execCxt = execCxt;
		}

		public NullaryExecutableOp<OutElmtType> getOp() {
			return op;
		}

		public void run() {
			op.execute(sink, execCxt);
			sink.close();
		}

	} // end of class OpRunnerThread
	
}
