package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVars;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpDedup;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFixedSolMap;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpProject;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnfold;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

public class ProjectPushDown implements HeuristicForLogicalOptimization
{

/**
 * Pushes project variables as much as possible towards the leaf nodes
 *  of a given logical plan.
 */
	@Override
	public LogicalPlan apply( LogicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if ( rootOp instanceof LogicalOpProject projectOp ) {
			return applyToPlanWithProjectAsRootOperator ( projectOp,
			                                              inputPlan.getSubPlan(0),
			                                              inputPlan );
		}
		else {
			// For any other type of root operator, simply apply
			// this heuristic recursively to all of the subplans.
			final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];
			boolean noChanges = true; // set to false if the heuristic changes any of the subplans
			for ( int i = 0; i < numberOfSubPlans; i++ ) {
				final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
				newSubPlans[i] = apply(oldSubPlan);
				if ( ! newSubPlans[i].equals(oldSubPlan) ) {
					noChanges = false;
				}
			}

			if ( noChanges )
				return inputPlan;

			return LogicalPlanUtils.createPlanWithSubPlans(rootOp, null, newSubPlans);
		}
	}

	protected LogicalPlan applyToPlanWithProjectAsRootOperator( final LogicalOpProject projectOp,
	                                                            final LogicalPlan subPlanUnderProject,
	                                                            final LogicalPlan inputPlan ) {
		final LogicalOperator childOpUnderProject = subPlanUnderProject.getRootOperator();
		final Worker worker = new Worker( projectOp, subPlanUnderProject, inputPlan );

		childOpUnderProject.visit(worker);
		final LogicalPlan createdPlan = worker.getCreatedPlan();
		if ( createdPlan != null ) return createdPlan;
		else
		{
			throw new IllegalArgumentException( childOpUnderProject.getClass().getName() );
		}
	}

	protected class Worker implements LogicalPlanVisitor {
		protected final LogicalOpProject projectOp;
		protected final LogicalPlan subPlanUnderProject;
		protected final LogicalPlan inputPlan;
		protected LogicalPlan createdPlan;

		public Worker( final LogicalOpProject projectOp, final LogicalPlan subPlanUnderProject, final LogicalPlan inputPlan ) {
			this.projectOp = projectOp;
			this.subPlanUnderProject = subPlanUnderProject;
			this.inputPlan = inputPlan;
		}

		public LogicalPlan getCreatedPlan() { return createdPlan; }

		@Override
		public void visit( final LogicalOpRequest<?, ?> op ) {
			createdPlan = createPlanForRequestUnderProject( projectOp,
			                                                op,
			                                                inputPlan );
		}

		@Override
		public void visit( final LogicalOpFixedSolMap op ) {
			// The project cannot be pushed under this operator.
			createdPlan = inputPlan;
		}

		@Override
		public void visit( final LogicalOpGPAdd op ) {
			createdPlan = createPlanForAddOpUnderProject( projectOp,
			                                              op,
			                                              subPlanUnderProject.getSubPlan(0),
			                                              inputPlan );
		}

		@Override
		public void visit( final LogicalOpGPOptAdd op ) {
			createdPlan = createPlanForAddOpUnderProject( projectOp,
			                                              op,
			                                              subPlanUnderProject.getSubPlan(0),
			                                              inputPlan );
		}

		@Override
		public void visit( final LogicalOpJoin op ) {
			createdPlan = createPlanForJoinUnderProject( projectOp,
			                                             subPlanUnderProject,
			                                             inputPlan );
		}

		@Override
		public void visit( final LogicalOpLeftJoin op ) {
			createdPlan = createPlanForLeftJoinUnderProject( projectOp,
			                                                 subPlanUnderProject.getSubPlan(1), // non-optional subplan
			                                                 subPlanUnderProject.getSubPlan(0), // optional subplan
			                                                 inputPlan );
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			createdPlan = createPlanForUnionUnderProject( projectOp, subPlanUnderProject );
		}

		@Override
		public void visit( final LogicalOpMultiwayJoin op ) {
			createdPlan = createPlanForJoinUnderProject( projectOp,
			                                             subPlanUnderProject,
			                                             inputPlan );
		}

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) {
			createdPlan = createPlanForMultiwayLeftJoinUnderProject( projectOp,
			                                                         subPlanUnderProject,
			                                                         inputPlan );
		}

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) {
			createdPlan = createPlanForUnionUnderProject( projectOp, subPlanUnderProject );
		}

		@Override
		public void visit( final LogicalOpFilter op ) {
			// Do not push the project operator below the filter operator.
			// FilterPushDown has priority over ProjectPushDown because pushing
			// filters reduces the number of solution mappings earlier, which
			// is typically more beneficial than reducing their width.
			createdPlan = inputPlan;
		}

		@Override
		public void visit( final LogicalOpBind op ) {
			createdPlan = createPlanForBindUnderProject( projectOp,
			                                             op,
			                                             subPlanUnderProject.getSubPlan(0),
			                                             inputPlan );
		}

		@Override
		public void visit( final LogicalOpUnfold op ) {
			createdPlan = createPlanForUnfoldUnderProject( projectOp,
			                                               op,
			                                               subPlanUnderProject.getSubPlan(0),
			                                               inputPlan );
		}

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) {
			createdPlan = createPlanForL2GOrG2LUnderProject( projectOp,
			                                                 op,
			                                                 subPlanUnderProject.getSubPlan(0),
			                                                 inputPlan );
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			createdPlan = createPlanForL2GOrG2LUnderProject( projectOp,
			                                                 op,
			                                                 subPlanUnderProject.getSubPlan(0),
			                                                 inputPlan );
		}

		@Override
		public void visit( final LogicalOpDedup op ) {
			createdPlan = createPlanForUnaryOpUnderProject( projectOp,
			                                                op,
			                                                subPlanUnderProject.getSubPlan(0) );
		}

		@Override
		public void visit( final LogicalOpProject op ) {
			createdPlan = createPlanForProjectUnderProject( projectOp,
			                                                op,
			                                                subPlanUnderProject.getSubPlan(0) );
		}

	} // end of Worker

	protected LogicalPlan createPlanForRequestUnderProject( final LogicalOpProject projectOp,
	                                                        final LogicalOpRequest<?,?> reqOp,
	                                                        final LogicalPlan inputPlan ) {
		// Pushing a project operator into request operator is not possible yet TODO #570
		return inputPlan;
	}

	protected LogicalPlan createPlanForUnionUnderProject( final LogicalOpProject projectOp,
	                                                      final LogicalPlan subPlanUnderProject ) {
		// simply pushes the project operator into every subplan under the
		// union operator and, then, applies this heuristic recursively to
		// each such subplan; in the end, collects all resulting subplans
		// again under a multiway union as new root operator
		final int numberOfSubPlansUnderUnion = subPlanUnderProject.numberOfSubPlans();
		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlansUnderUnion];
		for ( int i = 0; i < numberOfSubPlansUnderUnion; i++ ) {
			final LogicalPlan subPlanUnderUnion = subPlanUnderProject.getSubPlan(i);
			final LogicalPlan newSubPlanWithProjectAsRoot = new LogicalPlanWithUnaryRootImpl(projectOp, null, subPlanUnderUnion);
			final LogicalPlan newSubPlanWithProjectPushed = apply(newSubPlanWithProjectAsRoot);
			newSubPlans[i] = newSubPlanWithProjectPushed;
		}

		// If there is only one subplan under the union, then remove the union altogether.
		if ( numberOfSubPlansUnderUnion == 1 ) return newSubPlans[0];

		final LogicalOperator unionOp = subPlanUnderProject.getRootOperator(); // may be multiway union or binary union
		return LogicalPlanUtils.createPlanWithSubPlans(unionOp, null, newSubPlans);
	}

	protected LogicalPlan createPlanForBindUnderProject( final LogicalOpProject projectOp,
	                                                     final LogicalOpBind bindOp,
	                                                     final LogicalPlan subPlanUnderBind,
	                                                     final LogicalPlan inputPlan ) {
		// Check whether the project can be pushed under the bind operator.
		// This is only safe if all variables required to evaluate the bind
		// expressions are preserved by the projection. Otherwise, pushing
		// the project would remove variables needed by the bind.
		final Set<Var> varsInProject = projectOp.getVariables();
		final Set<Var> varsUsedInBind = new HashSet<>();

		for ( Expr expr : bindOp.getBindExpressions().getExprs().values() ) {
			varsUsedInBind.addAll(ExprVars.getVarsMentioned(expr));
		}

		if ( ! varsInProject.containsAll(varsUsedInBind) )
			return inputPlan;

		// The project can be pushed.
		return createPlanForUnaryOpUnderProject(projectOp, bindOp, subPlanUnderBind);
	}

	protected LogicalPlan createPlanForUnfoldUnderProject(
			final LogicalOpProject projectOp,
			final LogicalOpUnfold unfoldOp,
			final LogicalPlan subPlanUnderUnfold,
			final LogicalPlan inputPlan ) {
		// Check whether the project can be pushed under the given
		// unfold operator, which is possible only if none of the
		// variables assigned by the unfold operator are included in
		// the project variables.
		final Set<Var> varsInProject = projectOp.getVariables();
		final Var unfoldVar1 = unfoldOp.getVar1();
		final Var unfoldVar2 = unfoldOp.getVar2();
		if ( varsInProject.contains(unfoldVar1) )
			return inputPlan;
		if ( unfoldVar2 != null && varsInProject.contains(unfoldVar2) )
			return inputPlan;

		// The project can be pushed. In this case, create a new subplan with
		// the project as root operator on top of the subplan that was under
		// the unfold, and apply this heuristic recursively to this new subplan.
		final LogicalPlan newSubPlan1 = LogicalPlanUtils.createPlanWithSubPlans(
				projectOp,
				null,
				subPlanUnderUnfold );
		final LogicalPlan newSubPlan2 = apply(newSubPlan1);

		// Finally, put together the new plan with the unfold operator as root.
		return LogicalPlanUtils.createPlanWithSubPlans(unfoldOp, null, newSubPlan2);
	}

	/**
	 * Assumes that the given child operator is either a {@link LogicalOpLocalToGlobal}
	 * or a {@link LogicalOpGlobalToLocal}.
	 */
	protected LogicalPlan createPlanForL2GOrG2LUnderProject( final LogicalOpProject parentProjectOp,
	                                                         final UnaryLogicalOp childOp,
	                                                         final LogicalPlan subPlanUnderChildOp,
	                                                         final LogicalPlan inputPlan ) {
		// The current implementation does not try to push project operators
		// conditions under the vocabulary rewriting operators,
		// LogicalOpLocalToGlobal and LogicalOpGlobalToLocal.

		// For the time being, we only apply the project
		// push-down heuristic to the subplan that is under the
		// given vocabulary rewriting operator.
		return createPlanAfterPushingInSubPlan(parentProjectOp, childOp, subPlanUnderChildOp, inputPlan);
	}

	/**
	 * Assumes that the given child operator is either
	 * a {@link LogicalOpTPAdd},
	 * a {@link LogicalOpTPOptAdd},
	 * a {@link LogicalOpBGPAdd},
	 * a {@link LogicalOpBGPOptAdd},
	 * a {@link LogicalOpGPAdd}, or
	 * a {@link LogicalOpGPOptAdd}.
	 */
	protected LogicalPlan createPlanForAddOpUnderProject( final LogicalOpProject parentProjectOp,
	                                                      final UnaryLogicalOp childOp,
	                                                      final LogicalPlan subPlanUnderChildOp,
	                                                      final LogicalPlan inputPlan ) {
		// TODO: Consider pushing the project operator also into the pattern of the
		// xxAdd operators (e.g., GPAdd, GPOptAdd) where possible. This is not handled
		// currently, as such operators do not appear in the logical plans we optimize.
		// This optimization can be revisited in the future, taking into account that
		// for optional patterns (xxOptAdd), pushing projections may not always be safe.

		final ExpectedVariables expVarsInSubPlan = subPlanUnderChildOp.getExpectedVariables();
		final Set<Var> certainVarsInSubPlan = expVarsInSubPlan.getCertainVariables();
		final Set<Var> varsInProject = parentProjectOp.getVariables();

		// Can only push if all projected variables are guaranteed by subplan
		if ( ! certainVarsInSubPlan.containsAll(varsInProject) ) {
			return createPlanAfterPushingInSubPlan(parentProjectOp, childOp, subPlanUnderChildOp, inputPlan);
		}

		// Push the entire project
		final LogicalPlan newSubPlanUnderChildOp = new LogicalPlanWithUnaryRootImpl(
				parentProjectOp,
				null,
				subPlanUnderChildOp );

		final LogicalPlan newSubPlanUnderChildOpRewritten = apply(newSubPlanUnderChildOp);

		return new LogicalPlanWithUnaryRootImpl(childOp, null, newSubPlanUnderChildOpRewritten);
	}

	protected LogicalPlan createPlanForJoinUnderProject( final LogicalOpProject projectOp,
	                                                     final LogicalPlan subPlanUnderProject,
	                                                     final LogicalPlan inputPlan ) {
		final int numberOfSubPlansUnderJoin = subPlanUnderProject.numberOfSubPlans();

		// If there is only one subplan under the join, then remove the join altogether.
		if (numberOfSubPlansUnderJoin == 1) {
			final LogicalPlan subPlan = subPlanUnderProject.getSubPlan(0);
			final LogicalPlan subPlanAfterProjectPushDown = apply(subPlan);
			return new LogicalPlanWithUnaryRootImpl(projectOp, null, subPlanAfterProjectPushDown);
		}

		final Set<Var> varsInProject = projectOp.getVariables();
		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlansUnderJoin];

		// For each subplan under the join, compute the intersection between the
		// projected variables and the variables produced by that subplan.
		// If the intersection is non-empty, push a project operator with those
		// variables into that subplan.
		boolean noChanges = true; // set to false if something has changed in the plan
		for (int i = 0; i < numberOfSubPlansUnderJoin; i++) {
			final LogicalPlan subPlan = subPlanUnderProject.getSubPlan(i);

			final Set<Var> varsInSubPlan = subPlan.getExpectedVariables().getCertainVariables();

			// Compute intersection
			final Set<Var> varsForThisBranch = new HashSet<>(varsInProject);
			varsForThisBranch.retainAll(varsInSubPlan);

			LogicalPlan newSubPlan;

			if (varsForThisBranch.isEmpty()) {
				// No projection needed for this branch
				newSubPlan = apply(subPlan);
			} else {
				final LogicalOpProject branchProject = new LogicalOpProject(varsForThisBranch);
				final LogicalPlan withProject = new LogicalPlanWithUnaryRootImpl(branchProject, null, subPlan);

				newSubPlan = apply(withProject);
			}

			newSubPlans[i] = newSubPlan;
			if (!newSubPlan.equals(subPlan))
				noChanges = false;
		}

		if ( noChanges )
			return inputPlan;

		final LogicalOperator joinOp = subPlanUnderProject.getRootOperator();

		final LogicalPlan newJoin = LogicalPlanUtils.createPlanWithSubPlans(
				joinOp,
				null,
				newSubPlans );

		// Keep original project on top
		return new LogicalPlanWithUnaryRootImpl(projectOp, null, newJoin);
	}

	protected LogicalPlan createPlanForLeftJoinUnderProject( final LogicalOpProject projectOp,
	                                                         final LogicalPlan nonoptSubPlan,
	                                                         final LogicalPlan optSubPlan,
	                                                         final LogicalPlan inputPlan ) {
		// Pushes project operator over a left join operator by applying
		// it to the non-optional (left) subplan only. The projection is
		// restricted to variables that are both projected and certain in
		// the non-optional subplan.
		final Set<Var> varsInProject = projectOp.getVariables();

		// Create the new non-optional subplan.
		final LogicalPlan newNonOptSubPlan;
		final Set<Var> varsInNonOpt = nonoptSubPlan.getExpectedVariables().getCertainVariables();
		final Set<Var> varsForNonOpt = new HashSet<>(varsInProject);
		varsForNonOpt.retainAll(varsInNonOpt);

		if ( varsForNonOpt.isEmpty() ) {
			newNonOptSubPlan = apply(nonoptSubPlan);
		}
		else {
			final LogicalOpProject projectForNonOpt = new LogicalOpProject(varsForNonOpt);
			final LogicalPlan newSubPlanWithProjectAsRoot = new LogicalPlanWithUnaryRootImpl(projectForNonOpt, null, nonoptSubPlan);
			newNonOptSubPlan = apply(newSubPlanWithProjectAsRoot);
		}

		// Project should not be pushed to the optional subplan
		final LogicalPlan newOptSubPlan = apply(optSubPlan);

		if (    newNonOptSubPlan.equals(nonoptSubPlan)
		     && newOptSubPlan.equals(optSubPlan) ) {
			return inputPlan;
		}

		// Create the new rewritten plan to be returned.
		final LogicalPlan newSubPlanUnderProject = new LogicalPlanWithBinaryRootImpl(
			LogicalOpLeftJoin.getInstance(),
			null,
			newNonOptSubPlan,
			newOptSubPlan );

		return new LogicalPlanWithUnaryRootImpl( projectOp,
		                                         null,
		                                         newSubPlanUnderProject );
	}

	protected LogicalPlan createPlanForMultiwayLeftJoinUnderProject( final LogicalOpProject projectOp,
	                                                                 final LogicalPlan subPlanUnderProject,
	                                                                 final LogicalPlan inputPlan ) {
		// Only push the project operator into the non-optional subplan.
		// For the optional subplans, we only apply the heuristic recursively,
		// but do not push the project operator into them.
		final int numberOfSubPlansUnderJoin = subPlanUnderProject.numberOfSubPlans();
		final LogicalPlan[] newSubPlansUnderJoin = new LogicalPlan[numberOfSubPlansUnderJoin];

		final Set<Var> varsInProject = projectOp.getVariables();

		// Create the new non-optional subplan.
		final LogicalPlan oldNonOptSubPlan = subPlanUnderProject.getSubPlan(0);
		final LogicalPlan newNonOptSubPlan;
		final Set<Var> varsInNonOpt = oldNonOptSubPlan.getExpectedVariables().getCertainVariables();
		final Set<Var> varsForNonOpt = new HashSet<>(varsInProject);
		varsForNonOpt.retainAll(varsInNonOpt);

		if ( varsForNonOpt.isEmpty() ) {
			newNonOptSubPlan = apply(oldNonOptSubPlan);
		}
		else {
			final LogicalOpProject projectForNonOpt = new LogicalOpProject(varsForNonOpt);
			final LogicalPlan newSubPlanWithProjectAsRoot = new LogicalPlanWithUnaryRootImpl(projectForNonOpt, null, oldNonOptSubPlan);
			newNonOptSubPlan = apply(newSubPlanWithProjectAsRoot);
		}
		newSubPlansUnderJoin[0] = newNonOptSubPlan;

		// Now set up the new optional subplans of the join by
		// applying this heuristic recursively to each of them.
		boolean noChanges = true; // set to false if something has changed in the plan
		for ( int i = 1; i < numberOfSubPlansUnderJoin; i++ ) {
			final LogicalPlan oldSubPlan = subPlanUnderProject.getSubPlan(i);
			final LogicalPlan newSubPlan = apply(oldSubPlan);

			if ( newSubPlan.equals(oldSubPlan) ) {
				// If the current subplan did not change when applying
				// the heuristic to it, then simply keep that subplan.
				newSubPlansUnderJoin[i] = oldSubPlan;
			}
			else {
				// Otherwise, use the changed subplan and record that
				// at least one of subplans is indeed a changed one.
				newSubPlansUnderJoin[i] = newSubPlan;
				noChanges = false;
			}
		}

		// If nothing was changed, return the given input plan.
		if ( noChanges && newSubPlansUnderJoin[0].equals(oldNonOptSubPlan) )
			return inputPlan;

		// Create the new rewritten plan to be returned.
		final LogicalPlan newSubPlanUnderProject = new LogicalPlanWithNaryRootImpl(
			LogicalOpMultiwayLeftJoin.getInstance(),
			null,
			newSubPlansUnderJoin );

		return new LogicalPlanWithUnaryRootImpl( projectOp,
		                                         null,
		                                         newSubPlanUnderProject );
	}

	protected LogicalPlan createPlanForProjectUnderProject( final LogicalOpProject parentProjectOp,
	                                                        final LogicalOpProject childProjectOp,
	                                                        final LogicalPlan subPlanUnderChildProjectOp ) {
		final Set<Var> intersectionOfVars = new HashSet<>(childProjectOp.getVariables());
		intersectionOfVars.retainAll(parentProjectOp.getVariables());

		final LogicalPlan newPlan = new LogicalPlanWithUnaryRootImpl(
			new LogicalOpProject(intersectionOfVars),
			null,
			subPlanUnderChildProjectOp );

		return apply(newPlan);
	}



	protected LogicalPlan createPlanForUnaryOpUnderProject( final LogicalOpProject projectOp, final UnaryLogicalOp op, final LogicalPlan subPlanUnderOp ) {
		// Create a new subplan with the project as root operator on top of the
		// subplan that was under the given unary operator, and apply this
		// heuristic recursively to this new subplan.
		final LogicalPlan newSubPlan1 = LogicalPlanUtils.createPlanWithSubPlans(
				projectOp,
				null,
				subPlanUnderOp );
		final LogicalPlan newSubPlan2 = apply(newSubPlan1);

		// Put together the new plan with the given unary operator as root.
		return LogicalPlanUtils.createPlanWithSubPlans(op, null, newSubPlan2);
	}

	protected LogicalPlan createPlanAfterPushingInSubPlan( final LogicalOpProject parentProjectOp,
	                                                       final UnaryLogicalOp childOp,
	                                                       final LogicalPlan subPlanUnderChildOp,
	                                                       final LogicalPlan inputPlan ) {
		// Apply the heuristic recursively within the given subplan.
		final LogicalPlan newPlanUnderChildRoot = apply(subPlanUnderChildOp);

		// If the heuristic cannot be applied within the subplan (more
		// precisely, if there are no project operators in the subplan that can
		// be pushed), then we return the given input plan (instead of
		// recreating another, identical version of that plan).
		if ( newPlanUnderChildRoot.equals(subPlanUnderChildOp) ) {
			return inputPlan;
		}

		// After pushing project in the subplan, create a new plan.
		final LogicalPlan newPlanUnderProject = new LogicalPlanWithUnaryRootImpl(
				childOp,
				null,
				newPlanUnderChildRoot );
		final LogicalPlan newPlan = new LogicalPlanWithUnaryRootImpl(
				parentProjectOp,
				null,
				newPlanUnderProject );
		return newPlan;
	}

}
