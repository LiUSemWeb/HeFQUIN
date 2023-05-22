package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

public class PullUpLtgOverUnion implements HeuristicForLogicalOptimization {

	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];
		boolean noChanges = true; // set to false if the heuristic changes any of the subplans
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
			newSubPlans[i] = apply(oldSubPlan);
			if ( ! newSubPlans[i].equals(oldSubPlan) ) {
				noChanges = false;
			}
		}

		final LogicalPlan newPlan;
		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if ( noChanges )
			newPlan = inputPlan;
		else {
			newPlan = LogicalPlanUtils.createPlanWithSubPlans(rootOp, newSubPlans);
		}

		if ( checkIfLtgCanBeExtractedOverUnion(newPlan) ) {
			return extractLtgOverNaryOp( newPlan );
		}
		else {
			return newPlan;
		}
	}

	/**
	 * Check whether all root operator under the UNION operator use the same vocabulary mapping:
	 * 	 - The operator is a request, using vm
	 * 	 - If the operator is a filter, then under that filter there must be a request, using vm
	 * 	 - If the operator is a L2G operator, under the L2G operator, it must use vm.
	 */
	public static boolean checkIfLtgCanBeExtractedOverUnion( final LogicalPlan unionPlan ){
		final LogicalOperator rootOp = unionPlan.getRootOperator();
		if ( !(rootOp instanceof LogicalOpUnion || rootOp instanceof LogicalOpMultiwayUnion) ){
			return false;
		}

		final VocabularyMapping vm0 = getVocabularyMappingOfSubPlan( unionPlan.getSubPlan(0) );
		if ( vm0 == null ) {
			return false;
		}

		for ( int i = 1; i < unionPlan.numberOfSubPlans(); i++ ) {
			final VocabularyMapping vm = getVocabularyMappingOfSubPlan(unionPlan.getSubPlan(i));
			if ( vm == null || !vm0.equals(vm) ){
				return false;
			}
		}
		return true;
	}

	protected static LogicalPlan extractLtgOverNaryOp( final LogicalPlan inputPlan ) {
		final int numberOfSubPlansUnderUnion = inputPlan.numberOfSubPlans();
		final LogicalPlan[] newUnionSubPlans = new LogicalPlan[numberOfSubPlansUnderUnion];

		for (int i = 0; i < numberOfSubPlansUnderUnion; i++) {
			final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
			final LogicalPlan newSubPlan;
			if (oldSubPlan.getRootOperator() instanceof LogicalOpLocalToGlobal) {
				newSubPlan = oldSubPlan.getSubPlan(0);
			} else {
				newSubPlan = oldSubPlan;
			}

			newUnionSubPlans[i] = newSubPlan;
		}

		final LogicalPlan newNextPlan = new LogicalPlanWithNaryRootImpl((NaryLogicalOp) inputPlan.getRootOperator(), newUnionSubPlans);

		final LogicalOpLocalToGlobal l2g = (LogicalOpLocalToGlobal) inputPlan.getSubPlan(0).getRootOperator();
		return new LogicalPlanWithUnaryRootImpl(l2g, newNextPlan);
	}

	public static VocabularyMapping getVocabularyMappingOfSubPlan( final LogicalPlan subPlan ){
		final LogicalOperator subRootOp = subPlan.getRootOperator();
		if ( !(subRootOp instanceof LogicalOpRequest || subRootOp instanceof LogicalOpFilter || subRootOp instanceof LogicalOpLocalToGlobal) ) {
			return null;
		}

		if ( subRootOp instanceof LogicalOpLocalToGlobal ){
			return ((LogicalOpLocalToGlobal) subRootOp).getVocabularyMapping();
		}
		if ( subRootOp instanceof LogicalOpRequest ){
			return ((LogicalOpRequest<?, ?>) subRootOp).getFederationMember().getVocabularyMapping();
		}
		if ( subRootOp instanceof LogicalOpFilter ){
			if ( !( subPlan.getSubPlan(0).getRootOperator() instanceof LogicalOpRequest) ){
				return null;
			}
			else {
				return ((LogicalOpRequest<?, ?>) subPlan.getSubPlan(0).getRootOperator()).getFederationMember().getVocabularyMapping();
			}
		}
		return null;
	}

}
