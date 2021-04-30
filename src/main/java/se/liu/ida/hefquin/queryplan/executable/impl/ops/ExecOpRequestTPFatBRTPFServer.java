package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;

/**
 * Implementation of an operator to request a (complete) TPF from a brTPF server.
 * This implementation handles pagination of the TPF; that is, it requests all
 * the pages, one after another.
 */
public class ExecOpRequestTPFatBRTPFServer extends ExecOpGenericTriplePatternRequestWithTPF<BRTPFServer>
{
	public ExecOpRequestTPFatBRTPFServer( final TriplePatternRequest req, final BRTPFServer fm ) {
		super( req, fm );
	}

	@Override
	protected TPFResponse performRequest( final TPFRequest tpfReq, final FederationAccessManager fedAccessMgr ) {
		return fedAccessMgr.performRequest( tpfReq, fm );
	}
}
