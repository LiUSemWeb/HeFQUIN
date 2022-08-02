package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

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
	protected final boolean useOuterJoinSemantics;

	protected ExecOpGenericIndexNestedLoopsJoinWithRequestOps( final QueryType query,
	                                                           final MemberType fm,
	                                                           final boolean useOuterJoinSemantics ) {
		super(query, fm);
		this.useOuterJoinSemantics = useOuterJoinSemantics;
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
			final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		final CompletableFuture<?>[] futures = initiateProcessing(input, sink, execCxt);

		// wait for all the futures to be completed
		if ( futures.length > 0 ) {
			try {
				CompletableFuture.allOf(futures).get();
			}
			catch ( final InterruptedException e ) {
				throw new ExecOpExecutionException("interruption of the futures that run the executable operators", e, this);
			}
			catch ( final ExecutionException e ) {
				throw new ExecOpExecutionException("The execution of the futures that run the executable operators caused an exception.", e, this);
			}
		}
	}

	protected CompletableFuture<?>[] initiateProcessing(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		final CompletableFuture<?>[] futures = new CompletableFuture[input.size()];

		int i = 0;
		for ( final SolutionMapping sm : input.getSolutionMappings() ) {
			final CompletableFuture<?> f = initiateProcessing(sm, sink, execCxt);
			if ( f == null ) {
				// this may happen if the current solution mapping contains
				// a blank node for any of the variables that is used when
				// creating the request
				continue;
			}

			futures[i] = f;
			i++;
		}

		if ( i < futures.length ) {
			// This case may occur if we have skipped any of the
			// iteration steps of the previous loop because any
			// of the futures obtained in that loop was null.
			return Arrays.copyOf(futures, i);
		}
		else {
			return futures;
		}
	}

	protected CompletableFuture<?> initiateProcessing(
			final SolutionMapping sm,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt) throws ExecOpExecutionException
	{
		final NullaryExecutableOp reqOp = createExecutableRequestOperator(sm);
		if ( reqOp == null ) {
			// this may happen if the given solution mapping
			// contains a blank node for any of the variables
			// that is used when creating the request
			return null;
		}

		final Runnable processor = createProcessor(reqOp, sm, sink, execCxt);
		final ExecutorService execService = execCxt.getExecutorServiceForPlanTasks();
		if ( execService != null )
			return CompletableFuture.runAsync(processor, execService);
		else
			return CompletableFuture.runAsync(processor);
	}

	protected abstract NullaryExecutableOp createExecutableRequestOperator( SolutionMapping sm );

	protected Runnable createProcessor( final NullaryExecutableOp reqOp,
	                                    final SolutionMapping smFromInput,
	                                    final IntermediateResultElementSink outputSink,
	                                    final ExecutionContext execCxt ) {
		return new Runnable() {
			@Override
			public void run() {
				final MyIntermediateResultElementSink mySink;
				if ( useOuterJoinSemantics ) {
					mySink = new MyIntermediateResultElementSinkOuterJoin(outputSink, smFromInput);
				}
				else {
					mySink = new MyIntermediateResultElementSink(outputSink, smFromInput);
				}

				try {
					reqOp.execute(mySink, execCxt);
				}
				catch ( final ExecOpExecutionException e ) {
					throw new RuntimeException("Executing a request operator used by this index nested loops join caused an exception.", e);
				}

				mySink.flush();
			}
		};
	}


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

		public void flush() { }
	}

	protected static class MyIntermediateResultElementSinkOuterJoin extends MyIntermediateResultElementSink
	{
		protected boolean hasJoinPartner = false;

		public MyIntermediateResultElementSinkOuterJoin( final IntermediateResultElementSink outputSink,
		                                                 final SolutionMapping smFromInput ) {
			super(outputSink, smFromInput);
		}

		@Override
		public void send( final SolutionMapping smFromRequest ) {
			super.send(smFromRequest);
			hasJoinPartner = true;
		}

		public void flush() {
			if ( ! hasJoinPartner ) {
				outputSink.send(smFromInput);
			}
		}
    }

}
