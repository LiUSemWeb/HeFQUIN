package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.ResponseProcessingException;
import se.liu.ida.hefquin.engine.federation.access.impl.SynchronizedResponseProcessorBase;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Abstract base class to implement index nested loops joins by issuing
 * requests directly and, then, using response processors.
 */
public abstract class ExecOpGenericIndexNestedLoopsJoinWithRequests<
                            QueryType extends Query,
                            MemberType extends FederationMember,
                            ReqType extends DataRetrievalRequest,
                            RespType extends DataRetrievalResponse>
                   implements UnaryExecutableOp
{
	protected final QueryType query;
	protected final MemberType fm;

	public ExecOpGenericIndexNestedLoopsJoinWithRequests( final QueryType query, final MemberType fm ) {
		assert query != null;
		assert fm != null;

		this.query = query;
		this.fm = fm;
	}

	@Override
	public int preferredInputBlockSize() {
		// Since this algorithm processes the input solution mappings
		// sequentially (one at a time), and input block size of 1 may
		// reduce the response time of the overall execution process.
		return 1;  
	}

	@Override
	public void process(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt) throws ExecOpExecutionException
	{
		final List<MyResponseProcessor> respProcs = new ArrayList<>( input.size() );

		for ( final SolutionMapping sm : input.getSolutionMappings() ) {
			final ReqType req = createRequest(sm);
			final MyResponseProcessor respProc = createResponseProcessor(sm, sink);
			respProcs.add( respProc );

			try {
				issueRequest( req, respProc, execCxt.getFederationAccessMgr() );
			}
			catch ( final FederationAccessException e ) {
				throw new ExecOpExecutionException("Issuing a request caused an exception.", e, this);
			}
		}

		try {
			for ( final MyResponseProcessor respProc : respProcs ) {
				respProc.waitUntilResponseIsProcessed();
			}
		}
		catch ( final InterruptedException e ) {
			throw new ExecOpExecutionException("unexpected interruption of the main executing thread of this operator while waiting for some of its response processors", e, this);
		}
	}

	@Override
	public void concludeExecution(
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt )
	{
		// nothing to be done here
	}

	protected abstract ReqType createRequest( SolutionMapping sm );
	protected abstract MyResponseProcessor createResponseProcessor( SolutionMapping sm, IntermediateResultElementSink sink );
	protected abstract void issueRequest( ReqType req, MyResponseProcessor respProc, FederationAccessManager fedAccessMgr ) throws FederationAccessException;

	protected abstract class MyResponseProcessor extends SynchronizedResponseProcessorBase<RespType>
	{
		protected final SolutionMapping sm;
		protected final IntermediateResultElementSink sink;

		public MyResponseProcessor( final SolutionMapping sm, final IntermediateResultElementSink sink ) {
			this.sm = sm;
			this.sink = sink;
		}

		@Override
		protected void _process( final RespType response ) throws ResponseProcessingException {
			for ( final SolutionMapping fetchedSM : extractSolMaps(response) ) {
				final SolutionMapping out = SolutionMappingUtils.merge( sm, fetchedSM );
				sink.send(out);
			}
		}

		protected abstract Iterable<SolutionMapping> extractSolMaps( RespType response ) throws ResponseProcessingException;
	}

}
