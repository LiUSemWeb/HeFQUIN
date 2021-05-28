package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

import java.util.NoSuchElementException;

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
	public int getArity() {
		return 0;
	}

	@Override
	public ResultElementIterator getSubIterator(final int i) throws NoSuchElementException {
		throw new NoSuchElementException("Nullary Execution Plans do not have sub iterators");
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
