package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVars;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
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
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMinus;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpProject;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnfold;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

/**
 * Pushes project operators as much as possible towards the leaf nodes
 * of a given logical plan. This implementation works recursively in a
 * top-down manner such that, after pushing a project operator under
 * some other operator, the pushed project is considered again for
 * further pushing. Whether and how a particular project operator
 * can be pushed depends on the variables that are required and
 * produced by the operators in the plan.
 *
 * For unary operators such as bind and unfold, the projection is adjusted
 * before being pushed:
 * - Variables produced by the operator are removed from the pushed projection,
 *   as they are not required as input.
 * - Variables required to evaluate expressions of the operator are added,
 *   to ensure that the operator can still be evaluated correctly.
 *
 * If this adjusted projection is identical to the original projection,
 * the project operator is pushed entirely below the respective operator.
 * Otherwise, the project operator is split: an adjusted projection is pushed
 * below the operator, while the original projection remains on top to preserve
 * the final output schema.
 *
 * For operators with multiple subplans (e.g., joins), projections may be
 * pushed into individual subplans based on the variables required in each
 * branch, while ensuring that all necessary join variables are preserved.
 *
 * In cases where pushing is not possible without violating correctness,
 * the project operator remains in its original position.
 */
public class ProjectPushDown implements HeuristicForLogicalOptimization
{
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
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
		// Visit the operator directly under the project operator using
		// a worked that attempts to create a new plan by pushing the
		// project operator.
		final Worker worker = new Worker(projectOp, subPlanUnderProject, inputPlan);
		subPlanUnderProject.getRootOperator().visit(worker);

		final LogicalPlan createdPlan = worker.getCreatedPlan();
		if ( createdPlan != null ) return createdPlan;

		// We may end up here only if there is a bug in the worker.
		throw new IllegalArgumentException( subPlanUnderProject.getRootOperator().getClass().getName() );
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
			createdPlan = createPlanForFixedSolMapUnderProject(projectOp, op, inputPlan);
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
			                                                 subPlanUnderProject.getSubPlan(0), // non-optional subplan
			                                                 subPlanUnderProject.getSubPlan(1), // optional subplan
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
			createdPlan = createPlanForUnaryOpUnderProject( projectOp,
			                                                op,
			                                                subPlanUnderProject.getSubPlan(0) );
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			createdPlan = createPlanForL2GOrG2LUnderProject( projectOp,
			                                                op,
			                                                subPlanUnderProject.getSubPlan(0) );
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

		@Override
		public void visit( final LogicalOpMinus op ) {
			//unimplemented
		}

	} // end of Worker

	protected LogicalPlan createPlanForRequestUnderProject( final LogicalOpProject projectOp,
	                                                        final LogicalOpRequest<?,?> reqOp,
	                                                        final LogicalPlan inputPlan ) {
		// Pushing a project operator into request operator is not possible yet TODO #570
		return inputPlan;
	}

	protected LogicalPlan createPlanForFixedSolMapUnderProject( final LogicalOpProject projectOp,
	                                                            final LogicalOpFixedSolMap fixedSolMapOp,
	                                                            final LogicalPlan inputPlan ) {
		// Apply the projection directly to the solution mappings
		// produced by the fixed solution mapping operator.
		final Set<Var> projectedVars = projectOp.getVariables();
		final Binding b = fixedSolMapOp.getSolutionMapping().asJenaBinding();
		final Set<Var> bindingVars = b.varsMentioned();
		// Special case: If set of variables in solution mapping
		// equals set of projection variables; then simply drop
		// the project operator.
		if ( bindingVars.equals(projectedVars) ) {
			return inputPlan.getSubPlan(0);
		}

		// Create new LogicalOpFixedSolMap with a solution mapping
		// that only contains the variables in the original
		// LogicalOpFixedSolMap and in the projection variables.
		final BindingBuilder bb = Binding.builder();
		for ( final Var v : projectedVars ) {
			final Node value = b.get(v);
			if ( value != null )
				bb.add(v, value);
		}
		final Binding projected = bb.build();
		final SolutionMapping newSolMap = new SolutionMappingImpl(projected);

		return new LogicalPlanWithNullaryRootImpl(
			new LogicalOpFixedSolMap(newSolMap),
			null);
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
		// Attempts to push a project operator below a bind operator.
		//
		// The pushed projection is adjusted to ensure correctness:
		// - Variables assigned by the bind operator are removed, since they
		//   are produced by the bind and not needed as input.
		// - Variables required to evaluate the bind expressions are added,
		//   to ensure the bind can still be evaluated after pushdown.
		//
		// If this adjustment does not change the projection, the whole project
		// can be pushed below the bind.
		//
		// If the adjusted projection contains all the variables of the subplan
		// under the bind (including both the certain and the possible variables),
		// then there is no need to push the adjusted projection at all and the
		// plan can remain as is.
		//
		// Otherwise, the project is split: the adjusted projection is pushed below
		// the bind, while the original projection remains on top.

		// Compute pushed vars
		final Set<Var> pushedProjectVars = new HashSet<>(projectOp.getVariables());

		// Remove vars produced by bind
		pushedProjectVars.removeAll( bindOp.getBindExpressions().getVars() );

		// Add vars needed for evaluating bind
		for ( final Expr expr : bindOp.getBindExpressions().getExprs().values() ) {
			pushedProjectVars.addAll( ExprVars.getVarsMentioned(expr) );
		}

		// If nothing changes, push whole project
		if ( pushedProjectVars.equals(projectOp.getVariables()) )
			return createPlanForUnaryOpUnderProject(projectOp, bindOp, subPlanUnderBind);

		// If adjusted projection contains all variables of the subplan under bind,
		// return the plan as is
		final Set<Var> subPlanVars = new HashSet<>(subPlanUnderBind.getExpectedVariables().getCertainVariables());
		subPlanVars.addAll(subPlanUnderBind.getExpectedVariables().getPossibleVariables());
		if ( pushedProjectVars.containsAll(subPlanVars) ) {
			return inputPlan;
		}

		// Otherwise, split
		final LogicalPlan pushed = new LogicalPlanWithUnaryRootImpl(
			new LogicalOpProject(pushedProjectVars, projectOp.mayReduce()),
			null,
			subPlanUnderBind );

		final LogicalPlan newBind = LogicalPlanUtils.createPlanWithSubPlans(
			bindOp,
			null,
			pushed );

		return new LogicalPlanWithUnaryRootImpl(projectOp, null, newBind);
	}

	protected LogicalPlan createPlanForUnfoldUnderProject(
			final LogicalOpProject projectOp,
			final LogicalOpUnfold unfoldOp,
			final LogicalPlan subPlanUnderUnfold,
			final LogicalPlan inputPlan ) {
		// Attempts to push a project operator below an unfold operator.
		//
		// The pushed projection is adjusted similarly to the bind case:
		// - Variables assigned by the unfold operator are removed, since they
		//   are produced by the unfold.
		// - Variables required to evaluate the unfold expression are added,
		//   to ensure the unfold can still be evaluated after pushdown.
		//
		// If this adjustment does not change the projection, the whole project
		// can be pushed below the unfold.
		//
		// If the adjusted projection contains all the variables of the subplan
		// under the bind (including both the certain and the possible variables),
		// then there is no need to push the adjusted projection at all and the
		// plan can remain as is.
		//
		// Otherwise, the project is split: the adjusted projection is pushed below
		// the unfold, while the original projection remains on top.

		// Compute pushed vars
		final Set<Var> pushedProjectVars = new HashSet<>(projectOp.getVariables());

		// Remove vars produced by unfold
		pushedProjectVars.remove( unfoldOp.getVar1() );
		if ( unfoldOp.getVar2() != null ) {
			pushedProjectVars.remove( unfoldOp.getVar2() );
		}

		// Add vars needed for evaluating unfold
		pushedProjectVars.addAll( ExprVars.getVarsMentioned(unfoldOp.getExpr()) );

		// If nothing changes, push whole project
		if ( pushedProjectVars.equals(projectOp.getVariables()) )
			return createPlanForUnaryOpUnderProject(projectOp, unfoldOp, subPlanUnderUnfold);

		// If adjusted projection contains all variables of the subplan under bind,
		// return the plan as is
		final Set<Var> subPlanVars = new HashSet<>(subPlanUnderUnfold.getExpectedVariables().getCertainVariables());
		subPlanVars.addAll(subPlanUnderUnfold.getExpectedVariables().getPossibleVariables());
		if ( pushedProjectVars.containsAll(subPlanVars) ) {
			return inputPlan;
		}

		// Otherwise, split
		final LogicalPlan pushed = new LogicalPlanWithUnaryRootImpl(
			new LogicalOpProject(pushedProjectVars, false),
			null,
			subPlanUnderUnfold );

		final LogicalPlan newUnfold = LogicalPlanUtils.createPlanWithSubPlans(
			unfoldOp,
			null,
			pushed );

		return new LogicalPlanWithUnaryRootImpl(projectOp, null, newUnfold);
	}

	/**
	 * Assumes that the given child operator is either a {@link LogicalOpLocalToGlobal}
	 * or a {@link LogicalOpGlobalToLocal}.
	 */
	protected LogicalPlan createPlanForL2GOrG2LUnderProject( final LogicalOpProject projectOp,
	                                                        final UnaryLogicalOp childOp,
	                                                        final LogicalPlan subPlanUnderChildOp ) {
		// Swaps a project operator with a LocalToGlobal or GlobalToLocal operator.
		// There are no variable or semantic constraints that restrict pushing
		// a project operator past L2G/G2L operators. The result is a reordered
		// plan where the L2G/G2L operator is applied first, followed by the
		// project operator on top of its subplan.

		final LogicalPlan projectPlan = new LogicalPlanWithUnaryRootImpl(
			projectOp,
			null,
			subPlanUnderChildOp );

		return new LogicalPlanWithUnaryRootImpl(childOp, null, projectPlan);
	}

	/**
	 * Assumes that the given child operator is either
	 * a {@link LogicalOpGPAdd}, or
	 * a {@link LogicalOpGPOptAdd}.
	 */
	protected LogicalPlan createPlanForAddOpUnderProject( final LogicalOpProject parentProjectOp,
	                                                      final UnaryLogicalOp childOp,
	                                                      final LogicalPlan subPlanUnderChildOp,
	                                                      final LogicalPlan inputPlan ) {
		final ExpectedVariables expVarsInSubPlan = subPlanUnderChildOp.getExpectedVariables();
		final Set<Var> certainVarsInSubPlan = expVarsInSubPlan.getCertainVariables();
		final Set<Var> possibleVarsInSubPlan = expVarsInSubPlan.getPossibleVariables();

		final Set<Var> varsInSubPlan = new HashSet<>(certainVarsInSubPlan);
		varsInSubPlan.addAll(possibleVarsInSubPlan);

		// Case 1:
		// We can push the full projection below the GPAdd/GPOptAdd operator
		// only if:
		//	(1) the subplan guarantees all projected variables, and
		//	(2) the operator does not require any extra variables
		//	  (e.g., parameter variables would be lost by projection)
		if ( certainVarsInSubPlan.containsAll(parentProjectOp.getVariables())
		  && operatorDoesNotRequireExtraVars(childOp, parentProjectOp) ) {
			final LogicalPlan pushed = new LogicalPlanWithUnaryRootImpl(parentProjectOp, null, subPlanUnderChildOp);

			return new LogicalPlanWithUnaryRootImpl(childOp, null, apply(pushed));
		}

		// Case 2:
		// We attempt a partial pushdown of the projection.
		// We push down only:
		//	(1) variables from the projection that are needed by the subplan output
		//	(2) variables required by the operator (e.g., GPAdd parameters, optional bindings)
		//
		// The remaining projection is kept above the operator.
		final Set<Var> neededByProject = new HashSet<>(parentProjectOp.getVariables());
		neededByProject.retainAll(varsInSubPlan);

		final Set<Var> neededByOperator = new HashSet<>();
		if ( childOp instanceof LogicalOpGPAdd gpAdd && gpAdd.hasParameterVariables() ) {
			// GPAdd parameter variables must be preserved by the projection.
			// If they are not present in the outer projection, we must abort pushdown.
			final Set<Var> paramVars = new HashSet<>(gpAdd.getParameterVariables().values());

			if ( ! parentProjectOp.getVariables().containsAll(paramVars) )
				return inputPlan; // cannot push at all

			neededByOperator.addAll(paramVars);
		}
		else if ( childOp instanceof LogicalOpGPOptAdd ) {
			neededByOperator.addAll(possibleVarsInSubPlan);
		}

		neededByOperator.retainAll(varsInSubPlan);

		final Set<Var> pushDownVars = new HashSet<>();
		pushDownVars.addAll(neededByProject);
		pushDownVars.addAll(neededByOperator);

		if ( pushDownVars.isEmpty() ) {
				// No projection needed for this branch
				return inputPlan;
		}

		final LogicalPlan pushed = new LogicalPlanWithUnaryRootImpl(
			new LogicalOpProject(pushDownVars, false),
			null,
			subPlanUnderChildOp );

		final LogicalPlan rewrittenPushed = apply(pushed);

		final LogicalPlan newChild = new LogicalPlanWithUnaryRootImpl(
			childOp,
			null,
			rewrittenPushed );

		return new LogicalPlanWithUnaryRootImpl(parentProjectOp, null, newChild);
	}

	protected LogicalPlan createPlanForJoinUnderProject( final LogicalOpProject projectOp,
	                                                     final LogicalPlan subPlanUnderProject,
	                                                     final LogicalPlan inputPlan ) {
		// Pushes a projection below a join by splitting required variables per branch.
		// Each branch must retain:
		// - variables required for join alignment (joinVars)
		// - variables required by the parent projection

		final int numberOfSubPlansUnderJoin = subPlanUnderProject.numberOfSubPlans();

		// If there is only one subplan under the join, then remove the join altogether.
		if ( numberOfSubPlansUnderJoin == 1 ) {
			final LogicalPlan subPlan = subPlanUnderProject.getSubPlan(0);
			final LogicalPlan subPlanAfterProjectPushDown = apply(subPlan);
			return new LogicalPlanWithUnaryRootImpl(projectOp, null, subPlanAfterProjectPushDown);
		}

		final Set<Var> joinVars = computeJoinVars(subPlanUnderProject);
		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlansUnderJoin];

		// For each subplan under the join, compute which variables must be preserved locally:
		// - variables required by the top-level projection
		// - variables required to preserve join correctness in this branch
		//
		// The union of these variables is pushed down as a projection into the branch.
		boolean noChanges = true; // set to false if something has changed in the plan
		for (int i = 0; i < numberOfSubPlansUnderJoin; i++) {
			final LogicalPlan subPlan = subPlanUnderProject.getSubPlan(i);

			final Set<Var> certainVarsInSubPlan = subPlan.getExpectedVariables().getCertainVariables();
			final Set<Var> possibleVarsInSubPlan = subPlan.getExpectedVariables().getPossibleVariables();

			final Set<Var> varsInSubPlan = new HashSet<>(certainVarsInSubPlan);
			varsInSubPlan.addAll(possibleVarsInSubPlan);

			Set<Var> neededByProject = new HashSet<>(projectOp.getVariables());
			neededByProject.retainAll(varsInSubPlan);

			Set<Var> neededByJoin = new HashSet<>(joinVars);
			neededByJoin.retainAll(varsInSubPlan);

			Set<Var> pushDownVars = new HashSet<>();
			pushDownVars.addAll(neededByProject);
			pushDownVars.addAll(neededByJoin);

			final LogicalPlan newSubPlan;

			if ( pushDownVars.isEmpty() ) {
				// No projection needed for this branch
				newSubPlan = apply(subPlan);
			} else {
				final LogicalOpProject branchProject = new LogicalOpProject(pushDownVars, projectOp.mayReduce());
				final LogicalPlan withProject = new LogicalPlanWithUnaryRootImpl(branchProject, null, subPlan);

				newSubPlan = apply(withProject);
			}

			newSubPlans[i] = newSubPlan;
			if ( !newSubPlan.equals(subPlan) )
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

		// Create the new non-optional subplan.
		final LogicalPlan newNonOptSubPlan;
		final Set<Var> varsInNonOpt = nonoptSubPlan.getExpectedVariables().getCertainVariables();
		final Set<Var> varsForNonOpt = new HashSet<>(projectOp.getVariables());
		varsForNonOpt.retainAll(varsInNonOpt);

		if ( varsForNonOpt.isEmpty() ) {
			newNonOptSubPlan = apply(nonoptSubPlan);
		}
		else {
			final LogicalOpProject projectForNonOpt = new LogicalOpProject(varsForNonOpt, projectOp.mayReduce());
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
		// Only push the projectcreatePlanForL2GOrG2LUnderProject operator into the non-optional subplan.
		// For the optional subplans, we only apply the heuristic recursively,
		// but do not push the project operator into them.
		final int numberOfSubPlansUnderJoin = subPlanUnderProject.numberOfSubPlans();
		final LogicalPlan[] newSubPlansUnderJoin = new LogicalPlan[numberOfSubPlansUnderJoin];

		// Create the new non-optional subplan.
		final LogicalPlan oldNonOptSubPlan = subPlanUnderProject.getSubPlan(0);
		final LogicalPlan newNonOptSubPlan;
		final Set<Var> varsInNonOpt = oldNonOptSubPlan.getExpectedVariables().getCertainVariables();
		final Set<Var> varsForNonOpt = new HashSet<>(projectOp.getVariables());
		varsForNonOpt.retainAll(varsInNonOpt);

		if ( varsForNonOpt.isEmpty() ) {
			newNonOptSubPlan = apply(oldNonOptSubPlan);
		}
		else {
			final LogicalOpProject projectForNonOpt = new LogicalOpProject(varsForNonOpt, projectOp.mayReduce());
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
				// at least one of subplans is indeed a changed one.    return createPlanForUnaryOpUnderProject(projectOp, bindOp, subPlanUnderBind);
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
			new LogicalOpProject(intersectionOfVars, parentProjectOp.mayReduce()),
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

	protected Set<Var> computeJoinVars( final LogicalPlan joinPlan ) {
		final Map<Var, Integer> seenOnce = new HashMap<>();
		final Set<Var> joinVars = new HashSet<>();

		for ( int i = 0; i < joinPlan.numberOfSubPlans(); i++ ) {
			final LogicalPlan sp = joinPlan.getSubPlan(i);

			final Set<Var> vars = new HashSet<>();
			vars.addAll(sp.getExpectedVariables().getCertainVariables());
			vars.addAll(sp.getExpectedVariables().getPossibleVariables());

			for ( final Var v : vars ) {
				if ( ! seenOnce.containsKey(v) ) {
					seenOnce.put(v, i);
				} else {
					joinVars.add(v);
				}
			}
		}

		return joinVars;
	}

	protected boolean operatorDoesNotRequireExtraVars( final UnaryLogicalOp childOp, final LogicalOpProject projectOp ) {
		if ( childOp instanceof LogicalOpGPAdd gpAdd ) {
			if ( ! gpAdd.hasParameterVariables() ) return true;

			return projectOp.getVariables().containsAll(gpAdd.getParameterVariables().values());
		}

		return true;
	}

}