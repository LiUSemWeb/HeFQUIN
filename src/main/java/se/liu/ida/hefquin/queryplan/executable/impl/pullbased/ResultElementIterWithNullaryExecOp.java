package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.queryplan.executable.impl.ClosableIntermediateResultElementSink;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ResultElementIterWithNullaryExecOp extends ResultElementIterBase
{
	protected final OpRunnerThread opRunnerThread;

	public ResultElementIterWithNullaryExecOp( final NullaryExecutableOp op,
	                                           final ExecutionContext execCxt )
	{
		assert op != null;
		assert execCxt != null;

		opRunnerThread = new OpRunnerThread( op, sink, execCxt );
	}

	public NullaryExecutableOp getOp() {
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
		private final NullaryExecutableOp op;
		protected final ClosableIntermediateResultElementSink sink;
		protected final ExecutionContext execCxt;

		public OpRunnerThread( final NullaryExecutableOp op,
		                       final ClosableIntermediateResultElementSink sink,
		                       final ExecutionContext execCxt )
		{
			this.op = op;
			this.sink = sink;
			this.execCxt = execCxt;
		}

		public NullaryExecutableOp getOp() {
			return op;
		}

		public void run() {
			op.execute(sink, execCxt);
			sink.close();
		}

	} // end of class OpRunnerThread
	
}
