package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.FederationMember;

/**
 * Abstract base class to implement index nested loops joins by using request
 * operators. The possibility to rely on a request operator is particularly
 * useful in cases in which this operator implements paging, because the
 * alternative would be the need to re-implement paging again in the nested
 * loops join algorithm of potential subclasses of this base class. 
 *
 * For an abstract base class that issues requests directly (instead of using
 * request operators), use {@link BaseForExecOpIndexNestedLoopsJoinWithRequests}.
 */
public abstract class BaseForExecOpIndexNestedLoopsJoinWithRequestOps<QueryType extends Query,
                                                                      MemberType extends FederationMember>
              extends UnaryExecutableOpBaseWithBatching
{
	// Since this algorithm processes the input solution mappings
	// in parallel, we should use an input block size with which
	// we can leverage this parallelism. However, I am not sure
	// yet what a good value is; it probably depends on various
	// factors, including the load on the server and the degree
	// of parallelism in the FederationAccessManager.
	public final static int DEFAULT_BATCH_SIZE = 30;

	protected final QueryType query;
	protected final MemberType fm;
	protected final boolean useOuterJoinSemantics;

	// statistics
	protected long numberOfOutputMappingsProduced = 0L;
	protected int numberOfRequestOpsUsed = 0;
	protected ExecutableOperatorStats statsOfLastReqOp = null; // no statsOfFirstReqOp because the req.ops are running in separate threads

	protected BaseForExecOpIndexNestedLoopsJoinWithRequestOps( final QueryType query,
	                                                           final MemberType fm,
	                                                           final boolean useOuterJoinSemantics,
	                                                           final int batchSize,
	                                                           final boolean collectExceptions ) {
		super(batchSize, collectExceptions);

		assert query != null;
		assert fm != null;

		this.query = query;
		this.fm = fm;
		this.useOuterJoinSemantics = useOuterJoinSemantics;
	}

	protected BaseForExecOpIndexNestedLoopsJoinWithRequestOps( final QueryType query,
	                                                           final MemberType fm,
	                                                           final boolean useOuterJoinSemantics,
	                                                           final boolean collectExceptions ) {
		this(query, fm, useOuterJoinSemantics, DEFAULT_BATCH_SIZE, collectExceptions);
	}

	@Override
	protected void _processBatch( final List<SolutionMapping> input,
	                              final IntermediateResultElementSink sink,
	                              final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		final CompletableFuture<?>[] futures = initiateProcessing( input, sink, execCxt );

		// wait for all the futures to be completed
		if ( futures.length > 0 ) {
			try {
				CompletableFuture.allOf( futures ).get();
			}
			catch ( final InterruptedException e ) {
				throw new ExecOpExecutionException( "interruption of the futures that run the executable operators", e, this );
			}
			catch ( final ExecutionException e ) {
				throw new ExecOpExecutionException( "The execution of the futures that run the executable operators caused an exception.", e, this );
			}
		}
	}

	protected CompletableFuture<?>[] initiateProcessing(
			final List<SolutionMapping> input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt )
	{
		final CompletableFuture<?>[] futures = new CompletableFuture[input.size()];

		int i = 0;
		for ( final SolutionMapping sm : input ) {
			final CompletableFuture<?> f;
			try {
				f = initiateProcessing( sm, sink, execCxt );
			}
			catch ( final VariableByBlankNodeSubstitutionException e ) {
				// this may happen if the current solution mapping contains
				// a blank node for any of the variables that is used when
				// creating the request

				if ( useOuterJoinSemantics ) {
					numberOfOutputMappingsProduced++;
					sink.send( sm );
				}

				continue;
			}

			futures[i] = f;
			i++;
		}

		if ( i < futures.length ) {
			// This case may occur if we have skipped any of the
			// iteration steps of the previous loop because any
			// of the futures obtained in that loop was null.
			return Arrays.copyOf( futures, i );
		}
		else {
			return futures;
		}
	}

	protected CompletableFuture<?> initiateProcessing(
			final SolutionMapping sm,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt )
					throws VariableByBlankNodeSubstitutionException
	{
		final NullaryExecutableOp reqOp = createExecutableRequestOperator(sm);

		numberOfRequestOpsUsed++;

		final Runnable processor = createProcessor( reqOp, sm, sink, execCxt );
		final ExecutorService execService = execCxt.getExecutorServiceForPlanTasks();
		if ( execService != null )
			return CompletableFuture.runAsync(processor, execService);
		else
			return CompletableFuture.runAsync(processor);
	}

	protected abstract NullaryExecutableOp createExecutableRequestOperator( SolutionMapping sm )
			throws VariableByBlankNodeSubstitutionException;

	protected Runnable createProcessor( final NullaryExecutableOp reqOp,
	                                    final SolutionMapping smFromInput,
	                                    final IntermediateResultElementSink outputSink,
	                                    final ExecutionContext execCxt ) {
		return new Runnable() {
			@Override
			public void run() {
				final MyIntermediateResultElementSink mySink;
				if ( useOuterJoinSemantics ) {
					mySink = new MyIntermediateResultElementSinkOuterJoin( outputSink, smFromInput );
				}
				else {
					mySink = new MyIntermediateResultElementSink( outputSink, smFromInput );
				}

				try {
					reqOp.execute( mySink, execCxt );
				}
				catch ( final ExecOpExecutionException e ) {
					throw new RuntimeException( "Executing a request operator used by this index nested loops join caused an exception.", e );
				}

				mySink.flush();

				statsOfLastReqOp = reqOp.getStats();
			}
		};
	}

	@Override
	protected void _concludeExecution( final List<SolutionMapping> input,
	                                   final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		if ( input != null && ! input.isEmpty() ) {
			_processBatch(input, sink, execCxt);
		}
	}

	@Override
	public void resetStats() {
		super.resetStats();
		numberOfOutputMappingsProduced = 0L;
		numberOfRequestOpsUsed = 0;
		statsOfLastReqOp = null;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "queryAsString",      query.toString() );
		s.put( "fedMemberAsString",  fm.toString() );
		s.put( "numberOfOutputMappingsProduced", Long.valueOf(numberOfOutputMappingsProduced) );
		s.put( "numberOfRequestOpsUsed",         Integer.valueOf(numberOfRequestOpsUsed) );
		s.put( "statsOfLastReqOp",               statsOfLastReqOp );
		return s;
	}


	// ------- helper classes ------

	protected class MyIntermediateResultElementSink implements IntermediateResultElementSink
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
			numberOfOutputMappingsProduced++;
			outputSink.send( SolutionMappingUtils.merge( smFromInput, smFromRequest ) );
		}

		public void flush() { }
	}

	protected class MyIntermediateResultElementSinkOuterJoin extends MyIntermediateResultElementSink
	{
		protected boolean hasJoinPartner = false;

		public MyIntermediateResultElementSinkOuterJoin( final IntermediateResultElementSink outputSink,
		                                                 final SolutionMapping smFromInput ) {
			super( outputSink, smFromInput );
		}

		@Override
		public void send( final SolutionMapping smFromRequest ) {
			super.send(smFromRequest);
			hasJoinPartner = true;
		}

		public void flush() {
			if ( ! hasJoinPartner ) {
				numberOfOutputMappingsProduced++;
				outputSink.send( smFromInput );
			}
		}
    }

}
