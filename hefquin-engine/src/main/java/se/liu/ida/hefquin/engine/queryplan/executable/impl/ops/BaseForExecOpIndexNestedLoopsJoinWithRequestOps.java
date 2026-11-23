package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.FederationAccessException;

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
              extends BaseForUnaryExecOpWithCollectedInput
{
	private static final Logger log = LoggerFactory.getLogger( BaseForExecOpIndexNestedLoopsJoinWithRequestOps.class );
	protected final QueryType query;
	protected final MemberType fm;
	protected final boolean useOuterJoinSemantics;

	/**
	 * Used to collect the request operators created while processing
	 * the input solution mappings. We use a synchronized list to make
	 * the implementation thread safe, which is necessary because the
	 * {@link #setTerminationFlag()} method of this class is called by
	 * a different thread than the processing methods.
	 */
	protected final List<NullaryExecutableOp> reqOps =
			Collections.synchronizedList( new ArrayList<>() );

	/**
	 * Used to collect the completable futures created for executing
	 * the request operators and processing their responses. We use
	 * a synchronized list to make the implementation thread safe,
	 * which is necessary because the {@link #setTerminationFlag()}
	 * method of this class is called by a different thread than the
	 * processing methods.
	 */
	protected final List<CompletableFuture<?>> futures =
			Collections.synchronizedList( new ArrayList<>() );

	// statistics
	protected long numberOfOutputMappingsProduced = 0L;
	protected int numberOfRequestOpsUsed = 0;
	protected ExecutableOperatorStats statsOfLastReqOp = null; // no statsOfFirstReqOp because the req.ops are running in separate threads

	protected BaseForExecOpIndexNestedLoopsJoinWithRequestOps( final QueryType query,
	                                                           final MemberType fm,
	                                                           final boolean useOuterJoinSemantics,
	                                                           final boolean mayReduce,
	                                                           final int minimumInputBlockSize,
	                                                           final boolean collectExceptions,
	                                                           final QueryPlanningInfo qpInfo ) {
		super(mayReduce, minimumInputBlockSize, collectExceptions, qpInfo);

		log.info( "Initialized IndexNestedLoopsJoinWithRequestOps for query {} on federation member {}", query, fm );

		assert query != null;
		assert fm != null;

		this.query = query;
		this.fm = fm;
		this.useOuterJoinSemantics = useOuterJoinSemantics;
	}

	@Override
	public void setTerminationFlag() {
		// Set my own termination flag
		super.setTerminationFlag();

		// Set the termination flag of all request operators
		// created while executing this operator.
		synchronized (reqOps) {
			reqOps.forEach( reqOp -> reqOp.setTerminationFlag() );
		}

		// Cancel all completable futures created while
		// executing this operator.
		synchronized (futures) {
			futures.forEach( f -> f.cancel(true) );
		}
	}

	@Override
	protected void _processCollectedInput( final List<SolutionMapping> input,
	                                       final IntermediateResultElementSink sink,
	                                       final QueryProcContextExt ctx )
			throws ExecOpExecutionException
	{
		log.info( "Starting request-based index nested loops join with {} input mappings", input.size() );
		initiateProcessing( input, sink, ctx );
	}

	protected void initiateProcessing( final List<SolutionMapping> input,
	                                   final IntermediateResultElementSink sink,
	                                   final QueryProcContextExt ctx )
	{
		final ExecutorService execService = ctx.getExecutorServiceForPlanTasks();

		final Iterator<SolutionMapping> it = input.iterator();
		while ( it.hasNext() ) {
			final SolutionMapping sm = it.next();

			final NullaryExecutableOp reqOp;
			try {
				reqOp = createExecutableRequestOperator(sm);
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

			numberOfRequestOpsUsed++;

			final Runnable processor = createProcessor(reqOp, sm, sink, ctx);

			reqOps.add(reqOp);
			futures.add( CompletableFuture.runAsync(processor, execService) );

			// Before handling the next input solution mapping (if any)
			// we check whether the termination flag has been set in the
			// meantime. If that is the case, we ignore the rest of the
			// input solution mappings.
			if ( it.hasNext() && terminationFlagIsSet() ) return;
		}
	}

	protected abstract NullaryExecutableOp createExecutableRequestOperator( SolutionMapping sm )
			throws VariableByBlankNodeSubstitutionException;

	protected Runnable createProcessor( final NullaryExecutableOp reqOp,
	                                    final SolutionMapping smFromInput,
	                                    final IntermediateResultElementSink outputSink,
	                                    final QueryProcContextExt ctx ) {
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
					reqOp.execute(mySink, ctx);
				}
				catch ( final ExecOpExecutionException e ) {
					throw new MyRuntimeException( "Executing a request operator used by this index nested loops join caused an exception: " + e.getMessage(), e );
				}

				mySink.flush();

				statsOfLastReqOp = reqOp.getStats();
			}
		};
	}

	@Override
	protected void _concludeExecution( final List<SolutionMapping> input,
	                                   final IntermediateResultElementSink sink,
	                                   final QueryProcContextExt ctx )
			throws ExecOpExecutionException
	{
		log.info( "Completed request-based index nested loops join. Requests issued: {}, outputs produced: {}",
		numberOfRequestOpsUsed,
		numberOfOutputMappingsProduced );
		if ( input != null && ! input.isEmpty() ) {
			_processCollectedInput(input, sink, ctx);
		}

		// Now we wait for all the futures to be completed. To this
		// end we need to convert the list of futures into an array.
		final CompletableFuture<?>[] arr;
		synchronized (futures) {
			arr = futures.toArray( new CompletableFuture<?>[0] );
		}

		// And now we can wait.
		try {
			CompletableFuture.allOf(arr).get();
		}
		catch ( final InterruptedException e ) {
			final String msg = "Waiting for the request operators of this " +
					"index-NLJ at the federation member with service URI " +
					fm.getServiceURI() + " was interrupted with the " +
					"following message: " + e.getMessage();
			throw new ExecOpExecutionException(msg, e, this);
		}
		catch ( final ExecutionException e ) {
			// If the termination flag of this operator has been set and the
			// cause of this exception is a cancellation, then we ignore this
			// exception because it was caused by the fact that we have
			// canceled all 'futures' in 'setTerminationFlag()'.
			if (    e.getCause() instanceof CancellationException
			     && terminationFlagIsSet() )
				return;

			final String causeMsg;
			if ( e.getCause() instanceof MyRuntimeException ) {
				final Throwable cc = e.getCause().getCause();
				if ( cc instanceof ExecOpExecutionException &&
				     cc.getCause() instanceof FederationAccessException ccc )
					causeMsg = ccc.getMessage();
				else
					causeMsg = cc.getMessage();
			}
			else
				causeMsg = e.getMessage();

			final String msg = "Processing the request operators of this " +
					"index-NLJ at the federation member with service URI " +
					fm.getServiceURI() + " caused an exception with " +
					"the following message: " + causeMsg;
			throw new ExecOpExecutionException(msg, e, this);
		}
		finally {
			reqOps.clear();
			futures.clear();
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

	protected static class MyRuntimeException extends RuntimeException
	{
		private static final long serialVersionUID = 4145868219145957909L;

		public MyRuntimeException( final String msg,
		                           final ExecOpExecutionException cause ) {
			super(msg, cause);
		}
	}

}
