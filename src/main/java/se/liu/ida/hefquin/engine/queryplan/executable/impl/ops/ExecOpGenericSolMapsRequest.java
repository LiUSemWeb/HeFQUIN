package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.ResponseProcessingException;
import se.liu.ida.hefquin.engine.federation.access.ResponseProcessor;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public abstract class ExecOpGenericSolMapsRequest<ReqType extends DataRetrievalRequest,
                                                  MemberType extends FederationMember>
                extends ExecOpGenericRequest<ReqType,MemberType>
                implements ResponseProcessor<SolMapsResponse>
{
	private static class MyState {
		public boolean waitingForResponse = false;
		public boolean awaitedResponseProcessed = false;
	}
	private MyState state = new MyState();
	private IntermediateResultElementSink currentSink = null;

	public ExecOpGenericSolMapsRequest( final ReqType req, final MemberType fm ) {
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

		synchronized (state) {
			state.waitingForResponse = true;
			state.awaitedResponseProcessed = false;
		}

		try {
			issueRequest( execCxt.getFederationAccessMgr() );
		}
		catch ( final FederationAccessException e ) {
			throw new ExecOpExecutionException("Issuing the request caused an exception.", e, this);
		}

		// wait here until the response has been processed
		synchronized (state) {
			try {
				while ( ! state.awaitedResponseProcessed ) {
					state.wait();
				}
			}
			catch ( final InterruptedException e ) {
				throw new ExecOpExecutionException("unexpected interruption of the main executing thread of this request operator", e, this);
			}
		}
	}

	@Override
	public final void process( final SolMapsResponse response ) throws ResponseProcessingException
	{
		synchronized (state) {
			if ( ! state.waitingForResponse ) {
				throw new ResponseProcessingException("Call to process a response even if this request operator (type: " + getClass().getName() + ") is not waiting for a response.", response);
			}
			state.waitingForResponse = false;
		}

		process(response, currentSink);

		synchronized (state) {
			state.awaitedResponseProcessed = true;
			state.notifyAll();
		}
	}

	protected void process( final SolMapsResponse response, final IntermediateResultElementSink sink )
	{
		for ( SolutionMapping sm : response.getSolutionMappings() ) {
			sink.send( sm );
		}
	}

	protected abstract void issueRequest( FederationAccessManager fedAccessMgr ) throws FederationAccessException;
}
