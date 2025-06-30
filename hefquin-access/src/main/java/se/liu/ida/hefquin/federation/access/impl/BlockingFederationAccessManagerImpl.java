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
	protected AtomicLong counterNeo4jRequests   = new AtomicLong(0L);

	public BlockingFederationAccessManagerImpl(
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF,
			final Neo4jRequestProcessor reqProcNeo4j) {
		super(reqProcSPARQL, reqProcTPF, reqProcBRTPF, reqProcNeo4j);
	}

	@Override
	public CompletableFuture<SolMapsResponse> issueRequest( final SPARQLRequest req, final SPARQLEndpoint fm )
			throws FederationAccessException
	{
		counterSPARQLRequests.incrementAndGet();
		final SolMapsResponse response = reqProcSPARQL.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final TPFServer fm )
			throws FederationAccessException
	{
		counterTPFRequests.incrementAndGet();
		final TPFResponse response = reqProcTPF.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final BRTPFServer fm )
			throws FederationAccessException
	{
		counterTPFRequests.incrementAndGet();
		final TPFResponse response = reqProcTPF.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final BRTPFRequest req, final BRTPFServer fm )
			throws FederationAccessException
	{
		counterBRTPFRequests.incrementAndGet();
		final TPFResponse response = reqProcBRTPF.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<RecordsResponse> issueRequest( final Neo4jRequest req, final Neo4jServer fm )
			throws FederationAccessException
	{
		counterNeo4jRequests.incrementAndGet();
		final RecordsResponse response = reqProcNeo4j.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	protected void _resetStats() {
		counterSPARQLRequests.set(0L);
		counterTPFRequests.set(0L);
		counterBRTPFRequests.set(0L);
		counterNeo4jRequests.set(0L);
	}

	@Override
	protected FederationAccessStatsImpl _getStats() {
		return new FederationAccessStatsImpl( counterSPARQLRequests.get(),
		                                      counterTPFRequests.get(),
		                                      counterBRTPFRequests.get(),
		                                      counterNeo4jRequests.get(),
		                                      counterSPARQLRequests.get(),
		                                      counterTPFRequests.get(),
		                                      counterBRTPFRequests.get(),
		                                      counterNeo4jRequests.get() );
	}

	@Override
	public void shutdown() {
		// do nothing
	}

}
