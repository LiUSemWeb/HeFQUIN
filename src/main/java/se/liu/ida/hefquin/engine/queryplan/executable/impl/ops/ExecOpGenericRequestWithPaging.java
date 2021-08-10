package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.ResponseProcessingException;
import se.liu.ida.hefquin.engine.federation.access.ResponseProcessor;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Base class for implementations of request operators with requests
 * that have to be broken into multiple requests to handle paging.
 */
public abstract class ExecOpGenericRequestWithPaging<
                                  ReqType extends DataRetrievalRequest,
                                  MemberType extends FederationMember,
                                  PageReqType extends DataRetrievalRequest,
                                  PageRespType extends DataRetrievalResponse>
                extends ExecOpGenericRequest<ReqType,MemberType>
                implements ResponseProcessor<PageRespType>
{
	private static class MyState {
		public boolean waitingForResponse = false;
		public boolean awaitedResponseProcessed = false;
		public boolean lastPageConsumed = false;
	}
	private MyState state = new MyState();
	private IntermediateResultElementSink currentSink = null;

	public ExecOpGenericRequestWithPaging( final ReqType req, final MemberType fm ) {
		super( req, fm );
	}

	@Override
	public final void execute( final IntermediateResultElementSink sink,
	                           final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		if ( currentSink != null ) {
			throw new ExecOpExecutionException("Another execution process is already/still running for this request operator.", this);
		}
		currentSink = sink;

		int pageNumber = 0;
		boolean wasLastPage = false;
		while ( ! wasLastPage ) {
			synchronized (state) {
				state.waitingForResponse = true;
				state.awaitedResponseProcessed = false;
				state.lastPageConsumed = false; // put here even it is more logical to do this before the loop, but that would require another synchronized block
			}

			// create and issue the request for the next page (processing
			// the response is done in the 'process' method below, which
			// may be executed in a separate thread) 
			final PageReqType pageRequest = createPageRequest(pageNumber);
			try {
				issuePageRequest( pageRequest, execCxt.getFederationAccessMgr() );
			}
			catch ( final FederationAccessException e ) {
				throw new ExecOpExecutionException("Issuing a page request caused an exception.", e, this);
			}

			synchronized (state) {
				try {
					while ( ! state.awaitedResponseProcessed ) {
						state.wait();
					}
				}
				catch ( final InterruptedException e ) {
					throw new ExecOpExecutionException("unexpected interruption of the main executing thread of this request operator", e, this);
				}

				wasLastPage = state.lastPageConsumed;
			}

			++pageNumber;
		}

		currentSink = null;
	}

	@Override
	public final void process( final PageRespType response )
			throws ResponseProcessingException
	{
		if ( currentSink == null ) {
			throw new ResponseProcessingException("Call to process a response even if no execution process seems to be running for this request operator (type: " + getClass().getName() + ").", response);
		}

		synchronized (state) {
			if ( ! state.waitingForResponse ) {
				throw new ResponseProcessingException("Call to process a response even if this request operator (type: " + getClass().getName() + ") is not waiting for a response.", response);
			}
			state.waitingForResponse = false;
		}

		consumeResponse( response, currentSink );
		final boolean wasLastPage = isLastPage(response);

		synchronized (state) {
			state.lastPageConsumed = wasLastPage;
			state.awaitedResponseProcessed = true;
			state.notifyAll();
		}
	}

	protected abstract PageReqType createPageRequest( int pageNumber );

	protected abstract void issuePageRequest( PageReqType pageReq, FederationAccessManager fedAccessMgr ) throws FederationAccessException;

	protected abstract void consumeResponse( PageRespType response, IntermediateResultElementSink sink );

	protected abstract boolean isLastPage( PageRespType response );
}
