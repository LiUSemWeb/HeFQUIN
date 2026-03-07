package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

public class RemovePairsOfG2lAndL2g implements HeuristicForLogicalOptimization {

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
		if ( noChanges ) {
			newPlan = inputPlan;
		}
		else {
			final LogicalOperator rootOp = inputPlan.getRootOperator();
			newPlan = LogicalPlanUtils.createPlanWithSubPlans( rootOp,
			                                                   null,
			                                                   newSubPlans );
		}

		if ( checkIfG2lAndL2gPairsRemovable(newPlan) ) {
			return newPlan.getSubPlan(0).getSubPlan(0);
		}
		else {
			return newPlan;
		}
	}

	/**
	 * If a pair of g2l and l2g operators use the same vocabulary mapping, these two operators can be omitted
	 */
	public static boolean checkIfG2lAndL2gPairsRemovable( final LogicalPlan plan ){
		final LogicalOperator rootOp = plan.getRootOperator();
		if ( !(rootOp instanceof LogicalOpGlobalToLocal) ){
			return false;
		}

		final LogicalOperator subLop = plan.getSubPlan(0).getRootOperator();
		if ( !(subLop instanceof LogicalOpLocalToGlobal) ) {
			return false;
		}

		final VocabularyMapping vm0 = ((LogicalOpGlobalToLocal) rootOp).getVocabularyMapping();
		final VocabularyMapping vm1 = ((LogicalOpLocalToGlobal) subLop).getVocabularyMapping();
		return vm0.equals(vm1);
	}

}
