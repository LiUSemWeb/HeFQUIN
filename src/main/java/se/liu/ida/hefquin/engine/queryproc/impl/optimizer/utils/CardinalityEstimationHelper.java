package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithNullaryRootImpl;

public class CardinalityEstimationHelper
{
	public static PhysicalPlan formRequestPlan( final LogicalOpTPAdd lop ) {
		final FederationMember fm = lop.getFederationMember();
		final TriplePattern tp = lop.getTP();

		final DataRetrievalRequest req;
		if      ( fm instanceof SPARQLEndpoint ) req = new TriplePatternRequestImpl(tp);
		else if ( fm instanceof TPFServer )      req = new TPFRequestImpl(tp, 0);
		else if ( fm instanceof BRTPFServer )    req = new TPFRequestImpl(tp, 0);
		else {
			throw new IllegalArgumentException("Unsupported type of federation member (type: " + fm.getClass().getName() + ").");
		}

		return createRequestPlan(fm, req);
	}

	public static PhysicalPlan formRequestPlan( final LogicalOpBGPAdd lop ) {
		final FederationMember fm = lop.getFederationMember();

		final DataRetrievalRequest req;
		if ( fm.getInterface().supportsBGPRequests() ) {
			req = new BGPRequestImpl( lop.getBGP() );
		}
		else {
			throw new IllegalArgumentException("Unsupported type of federation member (type: " + fm.getClass().getName() + ").");
		}

		return createRequestPlan(fm, req);
	}


	// ------- internal helper methods -------

	protected static PhysicalPlan createRequestPlan( final FederationMember fm,
	                                                 final DataRetrievalRequest req )
	{
		final LogicalOpRequest<?,?>  lopReq = new LogicalOpRequest<>(fm, req);
		final PhysicalOpRequest<?,?> popReq = new PhysicalOpRequest<>(lopReq);

		return new PhysicalPlanWithNullaryRootImpl(popReq);
	}

}
