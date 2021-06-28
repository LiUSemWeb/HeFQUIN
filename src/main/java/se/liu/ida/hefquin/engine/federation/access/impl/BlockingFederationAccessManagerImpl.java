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
	public SolMapsResponse performRequest( final SPARQLRequest req, final SPARQLEndpoint fm ) {
		return reqProcSPARQL.performRequest( req, fm );
	}

	@Override
	public TPFResponse performRequest( final TPFRequest req, final TPFServer fm ) {
		return reqProcTPF.performRequest( req, fm );
	}

	@Override
	public TPFResponse performRequest( final TPFRequest req, final BRTPFServer fm ) {
		return reqProcTPF.performRequest( req, fm );
	}

	@Override
	public TPFResponse performRequest( final BRTPFRequest req, final BRTPFServer fm) {
		return reqProcBRTPF.performRequest( req, fm );
	}

	@Override
	public StringRetrievalResponse performRequest(Neo4jRequest req, Neo4jServer fm) {
		return reqProcNeo4j.performRequest(req, fm);
	}

}
