package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;

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
	protected void issuePageRequest( final TPFRequest req,
	                                 final FederationAccessManager fedAccessMgr )
			throws FederationAccessException
	{
		fedAccessMgr.issueRequest(req, fm, this);
	}

}
