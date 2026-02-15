package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
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
		if ( noChanges ) {
			newPlan = inputPlan;
		}
		else {
			final LogicalOperator rootOp = inputPlan.getRootOperator();
			newPlan = LogicalPlanUtils.createPlanWithSubPlans( rootOp,
			                                                   null,
			                                                   newSubPlans );
		}

		if ( checkIfLtgCanBeExtractedOverUnion(newPlan) ) {
			return extractLtgOverNaryOp(newPlan);
		}
		else {
			return newPlan;
		}
	}

	/**
	 * Check if l2g operator can be pulled up over union by checking:
	 * i) the root operator is a union (binary or multiway),
	 * ii) every subplan under this join has an l2g operator as its root, and
	 * iii) all these l2g operators have the same vocab.mapping
	 */
	public static boolean checkIfLtgCanBeExtractedOverUnion( final LogicalPlan unionPlan ){
		final LogicalOperator rootOp = unionPlan.getRootOperator();
		if ( !(rootOp instanceof LogicalOpUnion || rootOp instanceof LogicalOpMultiwayUnion) ){
			return false;
		}

		final LogicalOperator firstLop = unionPlan.getSubPlan(0).getRootOperator();
		if ( !(firstLop instanceof LogicalOpLocalToGlobal) ) {
			return false;
		}

		final VocabularyMapping vm0 = ((LogicalOpLocalToGlobal) firstLop).getVocabularyMapping();
		for ( int i = 1; i < unionPlan.numberOfSubPlans(); i++ ) {
			final LogicalOperator lop = unionPlan.getSubPlan(i).getRootOperator();
			if ( !(lop instanceof LogicalOpLocalToGlobal) ){
				return false;
			}

			final VocabularyMapping vm = ((LogicalOpLocalToGlobal) lop).getVocabularyMapping();
			if ( !( vm0.equals( vm ) ) ){
				return false;
			}
		}

		return true;
	}

	protected static LogicalPlan extractLtgOverNaryOp( final LogicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];

		for (int i = 0; i < numberOfSubPlans; i++) {
			final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
			final LogicalPlan newSubPlan;
			if (oldSubPlan.getRootOperator() instanceof LogicalOpLocalToGlobal) {
				newSubPlan = oldSubPlan.getSubPlan(0);
			} else {
				throw new IllegalArgumentException();
			}

			newSubPlans[i] = newSubPlan;
		}

		final LogicalPlan newNextPlan = LogicalPlanUtils.createPlanWithSubPlans(
				inputPlan.getRootOperator(),
				null,
				newSubPlans );

		final LogicalOpLocalToGlobal l2g = (LogicalOpLocalToGlobal) inputPlan.getSubPlan(0).getRootOperator();
		return new LogicalPlanWithUnaryRootImpl(l2g, null, newNextPlan);
	}

}
