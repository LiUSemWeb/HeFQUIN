package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;

/**
 * Implementation of an operator to request a (complete) TPF from a brTPF server.
 * This implementation handles pagination of the TPF; that is, it requests all
 * the pages, one after another.
 */
public class ExecOpRequestTPFatBRTPFServerWithTranslation extends ExecOpGenericTriplePatternRequestWithTranslation<BRTPFServer>
{
	public ExecOpRequestTPFatBRTPFServerWithTranslation( final TriplePatternRequest req, final BRTPFServer fm ) {
		super( req, fm );
	}
	
}
