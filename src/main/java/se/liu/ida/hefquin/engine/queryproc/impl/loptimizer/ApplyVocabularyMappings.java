package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
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
	 * Rewrites an initial logical plan into a second plan which incorporates translations of local to global vocabulary and request-operator rewriting.
	 * This method implements the rewriteLogPlan pseudocode of Helgesson's B.Sc thesis.
	 */
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		if (inputPlan.getRootOperator() instanceof LogicalOpRequest) {
			final LogicalOpRequest<?,?> requestOp = (LogicalOpRequest<?,?>) inputPlan.getRootOperator();
			
			if(requestOp.getFederationMember().getVocabularyMapping() != null) { // If fm has a vocabulary mapping vm
				final LogicalOpLocalToGlobal l2g = new LogicalOpLocalToGlobal(requestOp.getFederationMember().getVocabularyMapping());
				if (requestOp.getRequest() instanceof SPARQLRequest) {
					final LogicalPlan rw = LogicalOpUtils.rewriteToUseLocalVocabulary(requestOp);
					return new LogicalPlanWithUnaryRootImpl(l2g,rw);
				} else {
					throw new IllegalArgumentException( "The given plan is a non-SPARQL request." );
				}
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
				final LogicalPlanWithNaryRootImpl newPlan = new LogicalPlanWithNaryRootImpl( (NaryLogicalOp) inputPlan.getRootOperator(), rewrittenSubplans);
				return newPlan;
			} else {
				return inputPlan;
			}
		} else {
			throw new IllegalArgumentException("The given logical plan is not supported by this function because it has a root operator of type: " + inputPlan.getRootOperator().getClass().getName() );
		}
	}
}
