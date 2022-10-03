package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;

public class ApplyVocabularyMappings implements HeuristicForLogicalOptimization {

	/**
	 * Rewrites an initial logical plan into a second plan which incorporates translations of local to global vocabulary and request-operator rewriting.
	 * This method implements the rewriteLogPlan pseudocode of Helgesson's B.Sc thesis.
	 */
	@Override
	public LogicalPlan apply(LogicalPlan inputPlan) {
		if (inputPlan.getRootOperator() instanceof LogicalOpRequest) {
			final LogicalOpRequest request = (LogicalOpRequest) inputPlan.getRootOperator();
			
			if(request.getFederationMember().getVocabularyMapping() != null) { // If fm has a vocabulary mapping vm
				final LogicalOpLocalToGlobal l2g = new LogicalOpLocalToGlobal(request.getFederationMember().getVocabularyMapping());
				/*
				if (request.getRequest() instanceof SPARQLRequest) {
					final SPARQLRequest requestRequest = (SPARQLRequest) request.getRequest();
					final LogicalPlan rw = rewriteReqOf(requestRequest.getQueryPattern(), request.getFederationMember());
					return new LogicalPlanWithUnaryRootImpl(l2g,rw);
				} else {
					throw new IllegalArgumentException( "The given plan is a non-SPARQL request." );
				}
				*/
				// final LogicalPlan rw = LogicalOpUtils.rewriteReqOf(request) // , to call after the relevant function has been fixed
				//return new LogicalPlanWithUnaryRootImpl(l2g,rw);
				return inputPlan; // Temporary return for error suppression.s
			} else {
				return new LogicalPlanWithNullaryRootImpl(request);
			}
		} else if (inputPlan.getRootOperator() instanceof LogicalOpMultiwayJoin) {
			final List<LogicalPlan> rewrittenSubplans = new ArrayList<>();
			final Iterator<LogicalPlan> it = ((LogicalPlanWithNaryRoot) inputPlan).getSubPlans();
			while(it.hasNext()) {
				final LogicalPlan rewrittenSubplan = apply(it.next());
				rewrittenSubplans.add(rewrittenSubplan);
			}
			final LogicalOpMultiwayJoin newRoot = LogicalOpMultiwayJoin.getInstance();
			final LogicalPlanWithNaryRootImpl newPlan = new LogicalPlanWithNaryRootImpl(newRoot,rewrittenSubplans);
			return newPlan;
		} else if (inputPlan.getRootOperator() instanceof LogicalOpMultiwayUnion) {
			final List<LogicalPlan> rewrittenSubplans = new ArrayList<>();
			final Iterator<LogicalPlan> it = ((LogicalPlanWithNaryRoot) inputPlan).getSubPlans();
			while(it.hasNext()) {
				final LogicalPlan rewrittenSubplan = apply(it.next());
				rewrittenSubplans.add(rewrittenSubplan);
			}
			final LogicalOpMultiwayUnion newRoot = LogicalOpMultiwayUnion.getInstance();
			final LogicalPlanWithNaryRootImpl newPlan = new LogicalPlanWithNaryRootImpl(newRoot,rewrittenSubplans);
			return newPlan;
		} else {
			return inputPlan;
		}
	}
}
