package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

public class ResultElementIterWithNullaryExecOp extends ResultElementIterBase
{
	protected final MyOpRunnerThread opRunnerThread;

	public ResultElementIterWithNullaryExecOp( final NullaryExecutableOp op,
	                                           final QueryProcContextExt ctx )
	{
		super(ctx);

		assert op != null;

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
			op.execute(sink, ctx);
		}

	} // end of class OpRunnerThread
	
}
