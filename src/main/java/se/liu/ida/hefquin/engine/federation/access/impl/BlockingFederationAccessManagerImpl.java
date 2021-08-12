package se.liu.ida.hefquin.engine.federation.access.impl;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.federation.*;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.*;

/**
 * A very simple {@link FederationAccessManager}
 * that simply blocks for each request.
 */
public class BlockingFederationAccessManagerImpl extends FederationAccessManagerBase
{
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
		final SolMapsResponse response = reqProcSPARQL.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final TPFServer fm )
			throws FederationAccessException
	{
		final TPFResponse response = reqProcTPF.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final BRTPFServer fm )
			throws FederationAccessException
	{
		final TPFResponse response = reqProcTPF.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final BRTPFRequest req, final BRTPFServer fm )
			throws FederationAccessException
	{
		final TPFResponse response = reqProcBRTPF.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<StringResponse> issueRequest( final Neo4jRequest req, final Neo4jServer fm )
			throws FederationAccessException
	{
		final StringResponse response = reqProcNeo4j.performRequest(req, fm);
		return CompletableFuture.completedFuture(response);
	}

}
