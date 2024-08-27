package se.liu.ida.hefquin.engine.queryplan.executable;

import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * A specialization of the {@link ExecutableOperator} interface that
 * captures executable operators that do not consume any input produced
 * by other operators; hence, these operators are the leaf nodes in a
 * tree representation of query execution plans.
 */
public interface NullaryExecutableOp extends ExecutableOperator
{
	/**
	 * Executes this operator and sends the produced
	 * result elements (if any) to the given sink.
	 */
	void execute( IntermediateResultElementSink sink,
	              ExecutionContext execCxt ) throws ExecOpExecutionException;
}
