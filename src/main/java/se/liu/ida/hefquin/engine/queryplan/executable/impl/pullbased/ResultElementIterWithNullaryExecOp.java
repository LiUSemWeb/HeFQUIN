package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

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
		protected void _run() throws ExecutionException {
			op.execute(sink, execCxt);
		}

	} // end of class OpRunnerThread
	
}
