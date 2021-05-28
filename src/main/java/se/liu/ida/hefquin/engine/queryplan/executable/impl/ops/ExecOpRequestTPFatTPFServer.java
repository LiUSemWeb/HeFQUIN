package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanVisitor;

/**
 * Implementation of an operator to request a (complete) TPF from a TPF server.
 * This implementation handles pagination of the TPF; that is, it requests all
 * the pages, one after another.
 */
public class ExecOpRequestTPFatTPFServer extends ExecOpGenericTriplePatternRequestWithTPF<TPFServer>
{
	public ExecOpRequestTPFatTPFServer( final TriplePatternRequest req, final TPFServer fm ) {
		super( req, fm );
	}

	@Override
	protected TPFResponse performRequest( final TPFRequest tpfReq, final FederationAccessManager fedAccessMgr ) {
		return fedAccessMgr.performRequest( tpfReq, fm );
	}

	@Override
	public void visit(ExecutablePlanVisitor visitor) {
		visitor.visit(this);
	}
}
