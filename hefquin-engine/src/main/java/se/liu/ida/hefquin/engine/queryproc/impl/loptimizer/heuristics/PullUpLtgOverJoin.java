package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
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

		if ( checkIfLtgCanBeExtractedOverJoin(newPlan) ) {
			return PullUpLtgOverUnion.extractLtgOverNaryOp( newPlan );
		}
		else {
			return newPlan;
		}
	}

	/**
	 * Check if l2g operator can be pulled up over join by checking:
	 * i) the root operator is a join (binary or multiway),
	 * ii) every subplan under this join has an l2g operator as its root,
	 * iii) all these l2g operators have the same vocab.mapping, and
	 * iv) that vocab.mapping is an "equivalence-only" mapping.
	 */
	public static boolean checkIfLtgCanBeExtractedOverJoin( final LogicalPlan inputPlan ){
		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if ( !(rootOp instanceof LogicalOpJoin || rootOp instanceof LogicalOpMultiwayJoin) ){
			return false;
		}

		final LogicalOperator firstLop = inputPlan.getSubPlan(0).getRootOperator();
		if ( !(firstLop instanceof LogicalOpLocalToGlobal) ) {
			return false;
		}

		final VocabularyMapping vm0 = ((LogicalOpLocalToGlobal) firstLop).getVocabularyMapping();
		if ( !( vm0.isEquivalenceOnly() ) ) {
			return false;
		}

		for ( int i = 1; i < inputPlan.numberOfSubPlans(); i++ ) {
			final LogicalOperator lop = inputPlan.getSubPlan(i).getRootOperator();
			if ( !( lop instanceof LogicalOpLocalToGlobal ) ){
				return false;
			}

			final VocabularyMapping vm = ((LogicalOpLocalToGlobal) lop).getVocabularyMapping();
			if ( ! vm0.equals(vm) ) {
				return false;
			}
		}
		return true;
	}

}
