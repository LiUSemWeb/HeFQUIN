package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

public class PullUpLtgOverJoin implements HeuristicForLogicalOptimization {

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

		if ((inputPlan.getRootOperator() instanceof LogicalOpJoin || inputPlan.getRootOperator() instanceof LogicalOpMultiwayJoin )
				&& checkIfLtgCanBeExtractedOverJoin(newPlan)  ) {
			return PullUpLtgOverUnion.extractLtgOverNaryOp( newPlan );
		}
		else {
			return newPlan;
		}
	}

	/**
	 * Check whether all root operator under the JOIN operator use the same vocabulary mapping and contains equivalent mappings only:
	 * 	 - The operator is a request, using vm
	 * 	 - If the operator is a filter, then under that filter there must be a request, using vm
	 * 	 - If the operator is a L2G operator, under the L2G operator, it must use vm.
	 */
	public static boolean checkIfLtgCanBeExtractedOverJoin( final LogicalPlan inputPlan ){
		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if ( !(rootOp instanceof LogicalOpJoin || rootOp instanceof LogicalOpMultiwayJoin) ){
			return false;
		}

		final VocabularyMapping vm0 = PullUpLtgOverUnion.getVocabularyMappingOfSubPlan( inputPlan.getSubPlan(0) );
		if ( vm0 == null || !vm0.isEquivalenceOnly() ) {
			return false;
		}

		for ( int i = 1; i < inputPlan.numberOfSubPlans(); i++ ) {
			final VocabularyMapping vm = PullUpLtgOverUnion.getVocabularyMappingOfSubPlan(inputPlan.getSubPlan(i));
			if ( vm == null || !vm0.equals(vm) ){
				return false;
			}
		}
		return true;
	}

}
