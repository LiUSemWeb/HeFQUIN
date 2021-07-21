package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithNullaryRootImpl;

public class CardinalityEstimationHelper
{
	public static PhysicalPlan formRequestBasedOnTPofTPAdd( final LogicalOpTPAdd lop ) {
		final FederationMember fm = lop.getFederationMember();

		final DataRetrievalRequest req;
		if      ( fm instanceof SPARQLEndpoint ) req = new TriplePatternRequestImpl( lop.getTP() );
		else if ( fm instanceof TPFServer )      req = new TPFRequestImpl( lop.getTP(), 0 );
		else if ( fm instanceof BRTPFServer )    req = new TPFRequestImpl( lop.getTP(), 0 );
		else {
			throw new IllegalArgumentException("Unsupported federation member (type: " + fm.getClass().getName() + ").");
		}

		return createRequestPlan(fm, req);
	}

	public static PhysicalPlan formRequestBasedOnBGPofBGPAdd( final LogicalOpBGPAdd lop ) {
		final FederationMember fm = lop.getFederationMember();

		final DataRetrievalRequest req;
		if ( fm.getInterface().supportsBGPRequests() ) {
			req = new BGPRequestImpl( lop.getBGP() );
		}
		else {
			throw new IllegalArgumentException("Unsupported federation member (type: " + fm.getClass().getName() + ").");
		}

		return createRequestPlan(fm, req);
	}

	public static PhysicalPlan formRequestBasedOnPattern( final SPARQLGraphPattern p,
	                                                      final FederationMember fm ) {
		final DataRetrievalRequest req;
		if      ( fm instanceof SPARQLEndpoint ) req = new SPARQLRequestImpl(p);
		else if ( fm instanceof TPFServer )      req = new TriplePatternRequestImpl( (TriplePattern) p );
		else if ( fm instanceof BRTPFServer )    req = new TriplePatternRequestImpl( (TriplePattern) p );
		else {
			throw new IllegalArgumentException("Unsupported federation member type: " + fm.getClass().getName() );
		}

		return createRequestPlan(fm, req);
	}

	protected static PhysicalPlan createRequestPlan( final FederationMember fm,
	                                                 final DataRetrievalRequest req )
	{
		final LogicalOpRequest<?,?>  lopReq = new LogicalOpRequest<>(fm, req);
		final PhysicalOpRequest<?,?> popReq = new PhysicalOpRequest<>(lopReq);

		return new PhysicalPlanWithNullaryRootImpl(popReq);
	}

}
