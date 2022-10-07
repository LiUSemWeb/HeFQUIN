package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.engine.data.mappings.VocabularyMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;

public class ApplyVocabularyMappings implements HeuristicForLogicalOptimization {

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
		return ( newP.equals(p) ) ? inputPlan : LogicalOpUtils.rewriteReqOf(newP, fm);
	}
	
	
	/**
	 * Rewrites an initial logical plan into a second plan which incorporates translations of local to global vocabulary and request-operator rewriting.
	 * This method implements the rewriteLogPlan pseudocode of Helgesson's B.Sc thesis.
	 */
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		if (inputPlan.getRootOperator() instanceof LogicalOpRequest) {
			final LogicalOpRequest<?,?> requestOp = (LogicalOpRequest<?,?>) inputPlan.getRootOperator();
			if(requestOp.getFederationMember().getVocabularyMapping() != null) { // If fm has a vocabulary mapping vm
				final LogicalOpLocalToGlobal l2g = new LogicalOpLocalToGlobal(requestOp.getFederationMember().getVocabularyMapping());
				final LogicalPlan rw = rewriteToUseLocalVocabulary(inputPlan);
				return new LogicalPlanWithUnaryRootImpl(l2g,rw);
			} else {
				return inputPlan;
			}
		} else if ((inputPlan.getRootOperator() instanceof LogicalOpMultiwayJoin) || (inputPlan.getRootOperator() instanceof LogicalOpMultiwayUnion)) {
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
		} else {
			throw new IllegalArgumentException("The given logical plan is not supported by this function because it has a root operator of type: " + inputPlan.getRootOperator().getClass().getName() );
		}
	}
}
