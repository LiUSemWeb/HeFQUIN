package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;

/**
 * Implementation of an operator to request a (complete) TPF from a TPF server.
 * This implementation handles pagination of the TPF; that is, it requests all
 * the pages, one after another.
 */
public class ExecOpRequestTPFatTPFServer extends BaseForExecOpTriplePatternRequestWithTPF<TPFServer>
{
	public ExecOpRequestTPFatTPFServer( final TriplePatternRequest req,
	                                    final TPFServer fm,
	                                    final boolean collectExceptions ) {
		super( req, fm, collectExceptions );
	}

	@Override
	protected TPFResponse performPageRequest( final TPFRequest req,
	                                          final FederationAccessManager fedAccessMgr )
			throws FederationAccessException
	{
		return FederationAccessUtils.performRequest(fedAccessMgr, req, fm);
	}

}
