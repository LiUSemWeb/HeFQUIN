package se.liu.ida.hefquin.federation.access.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import se.liu.ida.hefquin.federation.*;
import se.liu.ida.hefquin.federation.access.*;
import se.liu.ida.hefquin.federation.access.impl.reqproc.*;

/**
 * A very simple {@link FederationAccessManager}
 * that simply blocks for each request.
 */
public class BlockingFederationAccessManagerImpl extends FederationAccessManagerBase2
{
	// stats
	protected AtomicLong counterSPARQLRequests  = new AtomicLong(0L);
	protected AtomicLong counterTPFRequests     = new AtomicLong(0L);
	protected AtomicLong counterBRTPFRequests   = new AtomicLong(0L);
	protected AtomicLong counterOtherRequests   = new AtomicLong(0L);
	protected AtomicLong counterRequests        = new AtomicLong(0L);

	public BlockingFederationAccessManagerImpl(
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF,
			final Neo4jRequestProcessor reqProcNeo4j) {
		super(reqProcSPARQL, reqProcTPF, reqProcBRTPF, reqProcNeo4j);
	}

	@Override
	public < ReqType extends DataRetrievalRequest,
	         RespType extends DataRetrievalResponse<?>,
	         MemberType extends FederationMember >
	CompletableFuture<RespType> issueRequest( final ReqType req,
	                                          final MemberType fm )
			throws FederationAccessException
	{
		final RequestProcessor<ReqType, RespType, MemberType> reqProc = getReqProc(req, fm);

		// update the statistics
		if ( reqProc instanceof SPARQLRequestProcessor )
			counterSPARQLRequests.incrementAndGet();
		else if ( reqProc instanceof TPFRequestProcessor )
			counterTPFRequests.incrementAndGet();
		else if ( reqProc instanceof BRTPFRequestProcessor )
			counterBRTPFRequests.incrementAndGet();
		else
			counterOtherRequests.incrementAndGet();

		final RespType response = reqProc.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	protected void _resetStats() {
		counterSPARQLRequests.set(0L);
		counterTPFRequests.set(0L);
		counterBRTPFRequests.set(0L);
		counterOtherRequests.set(0L);
	}

	@Override
	protected FederationAccessStatsImpl _getStats() {
		return new FederationAccessStatsImpl( counterSPARQLRequests.get(),
		                                      counterTPFRequests.get(),
		                                      counterBRTPFRequests.get(),
		                                      counterOtherRequests.get(),
		                                      counterSPARQLRequests.get(),
		                                      counterTPFRequests.get(),
		                                      counterBRTPFRequests.get(),
		                                      counterOtherRequests.get() );
	}

	@Override
	public void shutdown() {
		// do nothing
	}

}
