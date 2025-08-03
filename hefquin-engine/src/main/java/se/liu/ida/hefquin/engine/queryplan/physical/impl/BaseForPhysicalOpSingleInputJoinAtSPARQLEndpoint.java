package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;

public abstract class BaseForPhysicalOpSingleInputJoinAtSPARQLEndpoint
		extends BaseForPhysicalOpSingleInputJoin
{
	public BaseForPhysicalOpSingleInputJoinAtSPARQLEndpoint( final LogicalOpGPAdd lop ) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public BaseForPhysicalOpSingleInputJoinAtSPARQLEndpoint( final LogicalOpGPOptAdd lop ) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final QueryPlanningInfo qpInfo,
	                                       final ExpectedVariables... inputVars ) {
		final SPARQLGraphPattern gp;
		final FederationMember fm;
		final boolean useOuterJoin;

		if ( lop instanceof LogicalOpGPAdd gpAdd ) {
			gp = gpAdd.getPattern();
			fm = gpAdd.getFederationMember();
			useOuterJoin = false;
		}
		else if ( lop instanceof LogicalOpGPOptAdd gpOptAdd ) {
			gp = gpOptAdd.getPattern();
			fm = gpOptAdd.getFederationMember();
			useOuterJoin = true;
		}
		else {
			throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
		}

		if ( fm instanceof SPARQLEndpoint ep )
			return createExecOp(gp, ep, useOuterJoin, collectExceptions, qpInfo, inputVars);
		else
			throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
	}

	protected abstract UnaryExecutableOp createExecOp( SPARQLGraphPattern pattern,
	                                                   SPARQLEndpoint sparqlEndpoint,
	                                                   boolean useOuterJoinSemantics,
	                                                   boolean collectExceptions,
	                                                   QueryPlanningInfo qpInfo,
	        	                                       ExpectedVariables... inputVars );
}
