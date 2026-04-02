package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
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
		final Worker worker = new Worker( unionPlan );
		rootOp.visit(worker);

		return worker.getLtgCanBeExtractedOverUnion();
	}

	protected static class Worker implements LogicalPlanVisitor {
		protected final LogicalPlan unionPlan;
		protected boolean ltgCanBeExtractedOverUnion = false;

		public Worker( final LogicalPlan unionPlan ) {
			this.unionPlan = unionPlan;
		}

		public boolean getLtgCanBeExtractedOverUnion() { return ltgCanBeExtractedOverUnion; }

		@Override
		public void visit( final LogicalOpRequest<?, ?> op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpFixedSolMap op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpGPAdd op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpGPOptAdd op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpJoin op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpLeftJoin op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			ltgCanBeExtractedOverUnion = checkSubPlans();
		}

		@Override
		public void visit( final LogicalOpMultiwayJoin op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) {
			ltgCanBeExtractedOverUnion = checkSubPlans();
		}

		@Override
		public void visit( final LogicalOpFilter op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpBind op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpUnfold op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpDedup op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		@Override
		public void visit( final LogicalOpProject op ) {
			ltgCanBeExtractedOverUnion = false;
		}

		private boolean checkSubPlans() {
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

	} // end of Worker

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
