package se.liu.ida.hefquin.engine.federation.access.impl;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.federation.*;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.*;

/**
 * A very simple {@link FederationAccessManager}
 * that simply blocks for each request.
 */
public class BlockingFederationAccessManagerImpl extends FederationAccessManagerBase2
{
	// stats
	protected long counterSPARQLRequests  = 0L;
	protected long counterTPFRequests     = 0L;
	protected long counterBRTPFRequests   = 0L;
	protected long counterNeo4jRequests   = 0L;

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
		counterSPARQLRequests++;
		final SolMapsResponse response = reqProcSPARQL.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final TPFServer fm )
			throws FederationAccessException
	{
		counterTPFRequests++;
		final TPFResponse response = reqProcTPF.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final BRTPFServer fm )
			throws FederationAccessException
	{
		counterTPFRequests++;
		final TPFResponse response = reqProcTPF.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final BRTPFRequest req, final BRTPFServer fm )
			throws FederationAccessException
	{
		counterBRTPFRequests++;
		final TPFResponse response = reqProcBRTPF.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<RecordsResponse> issueRequest( final Neo4jRequest req, final Neo4jServer fm )
			throws FederationAccessException
	{
		counterNeo4jRequests++;
		final RecordsResponse response = reqProcNeo4j.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public void resetStats() {
		counterSPARQLRequests  = 0L;
		counterTPFRequests     = 0L;
		counterBRTPFRequests   = 0L;
		counterNeo4jRequests   = 0L;
	}

	@Override
	protected FederationAccessStatsImpl _getStats() {
		return new FederationAccessStatsImpl( counterSPARQLRequests,
		                                      counterTPFRequests,
		                                      counterBRTPFRequests,
		                                      counterNeo4jRequests,
		                                      counterSPARQLRequests,
		                                      counterTPFRequests,
		                                      counterBRTPFRequests,
		                                      counterNeo4jRequests );
	}

}
