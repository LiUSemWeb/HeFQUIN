package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ResultElementIterWithNullaryExecOp extends ResultElementIterBase
{
	protected final MyOpRunnerThread opRunnerThread;

	public ResultElementIterWithNullaryExecOp( final NullaryExecutableOp op,
	                                           final ExecutionContext execCxt )
	{
		super(execCxt);

		assert op != null;
		assert execCxt != null;

		opRunnerThread = new MyOpRunnerThread(op);
	}

	@Override
	public NullaryExecutableOp getOp() {
		return opRunnerThread.getOp();
	}

	@Override
	protected OpRunnerThread getOpRunnerThread() {
		return opRunnerThread;
	}


	protected class MyOpRunnerThread extends OpRunnerThread
	{
		private final NullaryExecutableOp op;

		public MyOpRunnerThread( final NullaryExecutableOp op ) {
			this.op = op;
		}

		@Override
		public NullaryExecutableOp getOp() {
			return op;
		}

		@Override
		public void run() {
			op.execute(sink, execCxt);
			sink.close();
		}

	} // end of class OpRunnerThread
	
}
