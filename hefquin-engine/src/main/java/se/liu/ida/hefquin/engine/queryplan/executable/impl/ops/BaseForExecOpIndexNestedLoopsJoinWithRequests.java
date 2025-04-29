package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Abstract base class to implement index nested loops joins by issuing
 * requests directly and, then, using response processors.
 *
 * An alternative option to this base class is the abstract base class
 * {@link BaseForExecOpIndexNestedLoopsJoinWithRequestOps} which relies
 * on executable request operators rather than issuing requests directly.
 * This option may come handy in cases in which a single request per 
 * input solution mapping is not enough because of paging.
 */
public abstract class BaseForExecOpIndexNestedLoopsJoinWithRequests<
                            QueryType extends Query,
                            MemberType extends FederationMember,
                            ReqType extends DataRetrievalRequest,
                            RespType extends DataRetrievalResponse<?>>
             extends BaseForExecOpIndexNestedLoopsJoin<QueryType,MemberType>
{
	public BaseForExecOpIndexNestedLoopsJoinWithRequests( final QueryType query,
	                                                      final MemberType fm,
	                                                      final boolean collectExceptions ) {
		super(query, fm, collectExceptions);
	}

	@Override
	public int preferredInputBlockSize() {
		// Since this algorithm processes the input solution mappings
		// in parallel, we should use an input block size with which
		// we can leverage this parallelism. However, I am not sure
		// yet what a good value is; it probably depends on various
		// factors, including the load on the server and the degree
		// of parallelism in the FederationAccessManager.
		return 30;
	}

	@Override
	protected void _process(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt) throws ExecOpExecutionException
	{
		final CompletableFuture<?>[] futures = new CompletableFuture[ input.size() ];

		int i = 0;
		for ( final SolutionMapping sm : input.getSolutionMappings() ) {
			// issue a request based on the current solution mapping
			final ReqType req;
			try {
				req = createRequest(sm);
			}
			catch ( final VariableByBlankNodeSubstitutionException e ) {
				// this may happen if the current solution mapping contains
				// a blank node for any of the variables that is used when
				// creating the request
				continue;
			}

			final CompletableFuture<RespType> futureResponse;
			try {
				futureResponse = issueRequest( req, execCxt.getFederationAccessMgr() );
			}
			catch ( final FederationAccessException e ) {
				throw new ExecOpExecutionException("Issuing a request caused an exception.", e, this);
			}

			// attach the processing of the response obtained for the request
			final MyResponseProcessor respProc = createResponseProcessor( sm, sink, this );
			futures[i] = futureResponse.thenAccept(respProc);
			++i;
		}

		final CompletableFuture<?>[] futures2;
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
		} catch (InterruptedException e) {
			throw new ExecOpExecutionException("interruption of the futures that perform the requests and process the responses", e, this);
		} catch (ExecutionException e) {
			throw new ExecOpExecutionException("The execution of the futures that perform the requests and process the responses caused an exception.", e, this);
		}
	}

	protected abstract ReqType createRequest( SolutionMapping sm ) throws VariableByBlankNodeSubstitutionException;

	protected abstract MyResponseProcessor createResponseProcessor( SolutionMapping sm, IntermediateResultElementSink sink, ExecutableOperator op );

	protected abstract CompletableFuture<RespType> issueRequest( ReqType req, FederationAccessManager fedAccessMgr ) throws FederationAccessException;


	// ---- helper classes -----

	protected abstract class MyResponseProcessor implements Consumer<RespType>
	{
		protected final SolutionMapping sm;
		protected final IntermediateResultElementSink sink;
		protected final ExecutableOperator op;

		public MyResponseProcessor( final SolutionMapping sm,
		                            final IntermediateResultElementSink sink,
		                            final ExecutableOperator op ) {
			this.sm = sm;
			this.sink = sink;
			this.op = op;
		}

		@Override
		public void accept( final RespType response ) {
			// if extractSolMaps throws an UnsupportedOperationDueToRetrievalError, we want to create an
			// ExecOpExecutionException and pass this exception to recordExceptionCaughtDuringExecution
			final Iterable<SolutionMapping> solutionMappings;
			try {
				solutionMappings = extractSolMaps( response );
			} catch( UnsupportedOperationDueToRetrievalError e ) {
				final ExecOpExecutionException ex = new ExecOpExecutionException( "Accessing the response caused an exception that indicates a data retrieval error (message: " + e.getMessage() + ").", e, op );
				recordExceptionCaughtDuringExecution( ex );
				return;
			}

			for ( final SolutionMapping fetchedSM : solutionMappings ) {
				final SolutionMapping out = SolutionMappingUtils.merge( sm, fetchedSM );
				sink.send( out );
			}
		}

		protected abstract Iterable<SolutionMapping> extractSolMaps( RespType response ) throws UnsupportedOperationDueToRetrievalError;
	}

}
