package se.liu.ida.hefquin.engine.queryplan.executable;

import se.liu.ida.hefquin.engine.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

/**
 * An exception that occurred during the execution of a specific operator.
 */
public class ExecOpExecutionException extends ExecutionException
{
	private static final long serialVersionUID = 3285677866147516456L;

	/** operator during whose execution this exception occurred */
	protected final ExecutableOperator op;

	public ExecOpExecutionException( final String message,
	                                 final Throwable cause,
	                                 final ExecutableOperator op ) {
		super(message, cause);
		assert op != null;
		this.op = op;
	}

	public ExecOpExecutionException( final Throwable cause,
	                                 final ExecutableOperator op ) {
		super(cause);
		assert op != null;
		this.op = op;
	}

	public ExecOpExecutionException( final String message,
	                                 final ExecutableOperator op ) {
		super(message);
		assert op != null;
		this.op = op;
	}

	public ExecOpExecutionException( final ExecutableOperator op ) {
		super();
		assert op != null;
		this.op = op;
	}

	/**
	 * Returns the {@link ExecutableOperator} during
	 * whose execution this exception occurred.
	 */
	public ExecutableOperator getOperator() {
		return op;
	}

}
