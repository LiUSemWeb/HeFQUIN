package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.query.TriplePattern;

public class ExecOpIndexNestedLoopsJoinTPF extends ExecOpGenericIndexNestedLoopsJoinWithTPFRequests<TPFServer>
{
	public ExecOpIndexNestedLoopsJoinTPF( final TriplePattern query, final TPFServer fm ) {
		super( query, fm );
	}

	@Override
	protected TriplesResponse performRequest( final TriplePatternRequest req, final FederationAccessManager fedAccessMgr ) {
		return fedAccessMgr.performRequest(req, fm);
	}

}
