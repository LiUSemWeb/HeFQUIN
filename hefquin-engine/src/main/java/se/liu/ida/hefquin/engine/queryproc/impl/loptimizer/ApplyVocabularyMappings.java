package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.data.mappings.VocabularyMappingUtils;
import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.base.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;

public class ApplyVocabularyMappings implements HeuristicForLogicalOptimization {
	/**
	 * Rewrites an initial logical plan into a second plan which incorporates translations of local to global vocabulary and request-operator rewriting.
	 * This method implements the rewriteLogPlan pseudocode of Helgesson's B.Sc thesis.
	 */
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		if ( inputPlan.getRootOperator() instanceof LogicalOpRequest requestOp ) {
			final VocabularyMapping vm = requestOp.getFederationMember().getVocabularyMapping();
			if ( vm != null) { // If fm has a vocabulary mapping vm
				final LogicalPlan newInputPlan = rewriteToUseLocalVocabulary(inputPlan);

				final LogicalOpLocalToGlobal l2g = new LogicalOpLocalToGlobal(vm);
				return new LogicalPlanWithUnaryRootImpl(l2g, newInputPlan);
			}
			else {
				return inputPlan;
			}
		}
		else if ((inputPlan.getRootOperator() instanceof LogicalOpMultiwayJoin) || (inputPlan.getRootOperator() instanceof LogicalOpMultiwayUnion)) {
			final List<LogicalPlan> rewrittenSubplans = new ArrayList<>();
			final Iterator<LogicalPlan> it = ((LogicalPlanWithNaryRoot) inputPlan).getSubPlans();
			boolean rewritten = false;
			while(it.hasNext()) {
				final LogicalPlan subPlan = it.next();
				final LogicalPlan rewrittenSubplan = apply(subPlan);
				rewrittenSubplans.add(rewrittenSubplan);
				if(!subPlan.equals(rewrittenSubplan)) {
					rewritten = true;
				}
			}
			if (rewritten) {
				return new LogicalPlanWithNaryRootImpl( (NaryLogicalOp) inputPlan.getRootOperator(), rewrittenSubplans);
			} else {
				return inputPlan;
			}
		}
		else if ( inputPlan.getRootOperator() instanceof LogicalOpFilter filterOp ) {
			final LogicalPlan rewrittenSubPlan = apply( inputPlan.getSubPlan(0) );
			// TODO: the expressions of 'filterOp' should be rewritten too
			return new LogicalPlanWithUnaryRootImpl(filterOp, rewrittenSubPlan);
		}
		else if ( inputPlan.getRootOperator() instanceof LogicalOpBind bindOp ) {
			final LogicalPlan rewrittenSubPlan = apply( inputPlan.getSubPlan(0) );
			// TODO: the expressions of 'bindOp' should be rewritten too
			return new LogicalPlanWithUnaryRootImpl(bindOp, rewrittenSubPlan);
		}
		else {
			throw new IllegalArgumentException("The given logical plan is not supported by this function because it has a root operator of type: " + inputPlan.getRootOperator().getClass().getName() );
		}
	}

	/**
	 * Rewrites the given logical plan with a request operator as root into
	 * a logical plan that uses the local vocabulary of the federation member of
	 * the request.
	 */
	public static LogicalPlan rewriteToUseLocalVocabulary( final LogicalPlan inputPlan ) {
		if(!(inputPlan.getRootOperator() instanceof LogicalOpRequest)) {
			throw new IllegalArgumentException( "Input plan does not have a request operator as root: " + inputPlan.getRootOperator().getClass().getName() );
		}
		final LogicalOpRequest<?, ?> reqOp = (LogicalOpRequest<?, ?>) inputPlan.getRootOperator();
		final FederationMember fm = reqOp.getFederationMember();
		if (fm.getVocabularyMapping() == null) { // If no vocabulary mapping, nothing to translate.
			return inputPlan;
		}

		if(!(reqOp.getRequest() instanceof SPARQLRequest)) {
			throw new IllegalArgumentException( "Request must be a SPARQLRequest: " + reqOp.getRequest().getClass().getName() );
		}

		final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();
		final SPARQLGraphPattern p = req.getQueryPattern();

		final SPARQLGraphPattern newP = VocabularyMappingUtils.translateGraphPattern(p, fm.getVocabularyMapping());
		return ( newP.equals(p) ) ? inputPlan : rewriteReqOf(newP, fm);
	}

	/**
	 * Creates a logical plan where all requests are TriplePatternRequests
	 * for use when a federation member's interface is a TPF-server.
	 */
	public static LogicalPlan rewriteReqOf( final SPARQLGraphPattern pattern, final FederationMember fm ) {
		// Right now there are just TPF-servers and SPARQL endpoints, but there may be more in the future.
		// For now, we will not assume that third types of interfaces will necessarily support all patterns.
	
		// For SPARQL endpoints, the whole graph pattern can be sent in a single request.
		if ( fm instanceof SPARQLEndpoint ) {
			final SPARQLRequest reqP = new SPARQLRequestImpl(pattern);
			final LogicalOpRequest<SPARQLRequest, SPARQLEndpoint> req = new LogicalOpRequest<>( (SPARQLEndpoint) fm, reqP );
			return new LogicalPlanWithNullaryRootImpl(req);
		}
		else if( pattern instanceof TriplePattern ) {
			if ( ! fm.getInterface().supportsTriplePatternRequests() ) {
				throw new IllegalArgumentException( "The given federation member has the following interface type which does not support triple pattern requests: " + fm.getInterface().getClass().getName() );
			}
	
			final TriplePatternRequest req = new TriplePatternRequestImpl( (TriplePattern) pattern );
			final LogicalOpRequest<TriplePatternRequest, FederationMember> reqOp = new LogicalOpRequest<>(fm,req);
			return new LogicalPlanWithNullaryRootImpl(reqOp);
		}
		else if( pattern instanceof BGP ) {
			final BGP bgp = (BGP) pattern;
	
			if ( fm.getInterface().supportsBGPRequests() ) {
				final BGPRequest req = new BGPRequestImpl(bgp);
				final LogicalOpRequest<BGPRequest, FederationMember> reqOp = new LogicalOpRequest<>(fm,req);
				return new LogicalPlanWithNullaryRootImpl(reqOp);
			}
	
			if ( ! fm.getInterface().supportsTriplePatternRequests() ) {
				throw new IllegalArgumentException( "The given federation member has the following interface type which does not support triple pattern requests: " + fm.getInterface().getClass().getName() );
			}
	
			final List<LogicalPlan> subPlans = new ArrayList<>();
			for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
				final TriplePatternRequest req = new TriplePatternRequestImpl(tp);
				final LogicalOpRequest<TriplePatternRequest, FederationMember> reqOp = new LogicalOpRequest<>(fm,req);
				return new LogicalPlanWithNullaryRootImpl(reqOp);
			}
	
			final LogicalOpMultiwayJoin mjRootOp = LogicalOpMultiwayJoin.getInstance();
			return new LogicalPlanWithNaryRootImpl(mjRootOp, subPlans);
		}
		else if( pattern instanceof SPARQLUnionPattern ) {
			final List<LogicalPlan> subPlans = new ArrayList<>();
			for ( final SPARQLGraphPattern subP : ((SPARQLUnionPattern) pattern).getSubPatterns() ) {
				final LogicalPlan subPlan = rewriteReqOf(subP,fm);
				subPlans.add(subPlan);
			}
	
			final LogicalOpMultiwayUnion muRootOp = LogicalOpMultiwayUnion.getInstance();
			return new LogicalPlanWithNaryRootImpl(muRootOp, subPlans);
		}
		else if( pattern instanceof SPARQLGroupPattern ) {
			final List<LogicalPlan> subPlans = new ArrayList<>();
			for ( final SPARQLGraphPattern subP : ((SPARQLGroupPattern) pattern).getSubPatterns() ) {
				final LogicalPlan subPlan = rewriteReqOf(subP,fm);
				subPlans.add(subPlan);
			}
	
			final LogicalOpMultiwayJoin mjRootOp = LogicalOpMultiwayJoin.getInstance();
			return new LogicalPlanWithNaryRootImpl(mjRootOp, subPlans);
		}
		else {
			throw new IllegalArgumentException( pattern.getClass().getName() );
		}
	}

}
