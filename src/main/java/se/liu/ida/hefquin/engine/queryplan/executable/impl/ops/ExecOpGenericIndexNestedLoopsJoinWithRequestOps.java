package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Abstract base class to implement index nested loops joins by using request
 * operators. The possibility to rely on a request operator is particularly
 * useful in cases in which this operator implements paging, because the
 * alternative would be the need to re-implement paging again in the nested
 * loops join algorithm of potential subclasses of this base class. 
 *
 * For an abstract base class that issues requests directly (instead of using
 * request operators), use {@link ExecOpGenericIndexNestedLoopsJoinWithRequests}.
 */
public abstract class ExecOpGenericIndexNestedLoopsJoinWithRequestOps<
                                                    QueryType extends Query,
                                                    MemberType extends FederationMember>
              extends ExecOpGenericIndexNestedLoopsJoinBase<QueryType,MemberType>
{
	public ExecOpGenericIndexNestedLoopsJoinWithRequestOps( final QueryType query, final MemberType fm ) {
		super(query, fm);
	}

	@Override
	public int preferredInputBlockSize() {
		// Since this algorithm processes the input solution mappings
		// sequentially (one at a time), an input block size of 1 may
		// reduce the response time of the overall execution process.
		return 1;
	}

	@Override
	protected void _process(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt) throws ExecOpExecutionException
	{
		@SuppressWarnings("unchecked")
		final CompletableFuture<Void>[] futures = new CompletableFuture[input.size()];

		int i = 0;
		for ( final SolutionMapping sm : input.getSolutionMappings() ) {
			final CompletableFuture<Void> f = initiateProcessing(sm, sink, execCxt);
			if ( f == null ) {
				// this may happen if the current solution mapping contains
				// a blank node for any of the variables that is used when
				// creating the request
				continue;
			}

			futures[i] = f;
			++i;
		}

		final CompletableFuture<Void>[] futures2;
		if ( i < futures.length ) {
			// This case may occur if we have skipped any of the
			// iteration steps of the previous loop because any
			// of the requests created in the loop was null.
			futures2 = Arrays.copyOf(futures, i);
		}
		else {
			futures2 = futures;
		}

		// wait for all the futures to be completed
		try {
			CompletableFuture.allOf(futures2).get();
		}
		catch ( final InterruptedException e ) {
			throw new ExecOpExecutionException("interruption of the futures that run the executable operators", e, this);
		}
		catch ( final ExecutionException e ) {
			throw new ExecOpExecutionException("The execution of the futures that run the executable operators.", e, this);
		}
	}

	protected CompletableFuture<Void> initiateProcessing(
			final SolutionMapping sm,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt) throws ExecOpExecutionException
	{
		final NullaryExecutableOp reqOp = createExecutableRequestOperator(sm);
		if ( reqOp == null ) {
			return null;
		}

		final IntermediateResultElementSink mySink = new MyIntermediateResultElementSink(sink, sm);
		return CompletableFuture.runAsync( () -> {
			try {
				reqOp.execute(mySink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				throw new RuntimeException("Executing a request operator used by this index nested loops join caused an exception.", e);
			}
		});
	}

	protected abstract NullaryExecutableOp createExecutableRequestOperator( SolutionMapping sm );


	// ------- helper classes ------

	protected static class MyIntermediateResultElementSink implements IntermediateResultElementSink
	{
		protected final IntermediateResultElementSink outputSink;
		protected final SolutionMapping smFromInput;

		public MyIntermediateResultElementSink( final IntermediateResultElementSink outputSink,
		                                        final SolutionMapping smFromInput ) {
			this.outputSink = outputSink;
			this.smFromInput = smFromInput;
		}

		@Override
		public void send( final SolutionMapping smFromRequest ) {
			outputSink.send( SolutionMappingUtils.merge(smFromInput,smFromRequest) );
		}
    } // end of helper class MyIntermediateResultElementSink

}
