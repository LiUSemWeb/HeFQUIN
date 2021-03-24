package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.TriplesToSolMapsConverter;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.query.TriplePattern;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedQueryPatternUtils;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedTriplePattern;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.federation.access.impl.req.BindingsRestrictedTriplePatternRequestImpl;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;


import java.util.Iterator;
import java.util.Set;

public class ExecOpBindJoinBRTPF extends
		ExecOpGenericBindJoinWithTriplesRequests<TriplePattern, BRTPFServer, TriplePatternRequest>
{
	public ExecOpBindJoinBRTPF( final TriplePattern query, final BRTPFServer fm ) {
		super( query, fm );
	}

	@Override
	protected TriplePatternRequest createRequest( final TriplePattern query) {
		return new TriplePatternRequestImpl(query);
	}

	@Override
	protected Iterator<? extends SolutionMapping> convert(final Iterator<? extends Triple> itTriples, final TriplePatternRequest req ) {
		return TriplesToSolMapsConverter.convert( itTriples, req.getQueryPattern());
	}

	@Override
	protected TriplesResponse performRequest( final TriplePatternRequest req, final FederationAccessManager fedAccessMgr ) {
		return fedAccessMgr.performRequest(req, fm);
	}
}
