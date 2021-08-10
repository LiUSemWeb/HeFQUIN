package se.liu.ida.hefquin.engine.federation.access.impl;

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
	public void issueRequest( final SPARQLRequest req, final SPARQLEndpoint fm, final ResponseProcessor<SolMapsResponse> respProc )
			throws FederationAccessException
	{
		final SolMapsResponse response = reqProcSPARQL.performRequest(req, fm);
		respProc.process(response);
	}

	@Override
	public void issueRequest( final TPFRequest req, final TPFServer fm, final ResponseProcessor<TPFResponse> respProc )
			throws FederationAccessException
	{
		final TPFResponse response = reqProcTPF.performRequest(req, fm);
		respProc.process(response);
	}

	@Override
	public void issueRequest( final TPFRequest req, final BRTPFServer fm, final ResponseProcessor<TPFResponse> respProc )
			throws FederationAccessException
	{
		final TPFResponse response = reqProcTPF.performRequest(req, fm);
		respProc.process(response);
	}

	@Override
	public void issueRequest( final BRTPFRequest req, final BRTPFServer fm, final ResponseProcessor<TPFResponse> respProc )
			throws FederationAccessException
	{
		final TPFResponse response = reqProcBRTPF.performRequest(req, fm);
		respProc.process(response);
	}

	@Override
	public void issueRequest( final Neo4jRequest req, final Neo4jServer fm, final ResponseProcessor<StringResponse> respProc )
			throws FederationAccessException
	{
		final StringResponse response = reqProcNeo4j.performRequest(req, fm);
		respProc.process(response);
	}

}
