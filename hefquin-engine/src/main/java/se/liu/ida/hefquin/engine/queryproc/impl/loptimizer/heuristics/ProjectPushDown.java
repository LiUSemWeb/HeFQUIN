package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.Collection;
import java.util.HashSet;
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
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
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
				if ( ! newSubPlans[i].isSamePlan(oldSubPlan) ) {
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
			createdPlan = createPlanForJoinLikeOpUnderProject( projectOp,
			                                                   subPlanUnderProject,
			                                                   inputPlan );
		}

		@Override
		public void visit( final LogicalOpLeftJoin op ) {
			createdPlan = createPlanForJoinLikeOpUnderProject( projectOp,
			                                                   subPlanUnderProject,
			                                                   inputPlan );
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			createdPlan = createPlanForUnionUnderProject( projectOp, subPlanUnderProject );
		}

		@Override
		public void visit( final LogicalOpMultiwayJoin op ) {
			createdPlan = createPlanForJoinLikeOpUnderProject( projectOp,
			                                                   subPlanUnderProject,
			                                                   inputPlan );
		}

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) {
			createdPlan = createPlanForJoinLikeOpUnderProject( projectOp,
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
			createdPlan = createPlanForJoinLikeOpUnderProject( projectOp,
			                                                 subPlanUnderProject,
			                                                 inputPlan );
		}

	} // end of Worker

	/**
	 * Pushing a project operator into request operator is not possible yet TODO #570
	*/
	protected LogicalPlan createPlanForRequestUnderProject( final LogicalOpProject projectOp,
	                                                        final LogicalOpRequest<?,?> reqOp,
	                                                        final LogicalPlan inputPlan ) {
		return inputPlan;
	}

	/**
	 * Applies a project operator to a fixed solution mapping operator.
	 *
	 * If all variables in the solution mapping are retained by the projection,
	 * the project operator is redundant and is removed.
	 * Otherwise, a new fixed solution mapping is created that contains only
	 * the projected variables.
	 */
	protected LogicalPlan createPlanForFixedSolMapUnderProject( final LogicalOpProject projectOp,
	                                                            final LogicalOpFixedSolMap fixedSolMapOp,
	                                                            final LogicalPlan inputPlan ) {
		final Set<Var> projectedVars = projectOp.getVariables();
		final Binding b = fixedSolMapOp.getSolutionMapping().asJenaBinding();
		final Set<Var> bindingVars = b.varsMentioned();
		// Special case: If set of variables in the solution mapping
		// is a subset of projection variables; then simply drop
		// the project operator.
		if ( projectedVars.containsAll(bindingVars) ) {
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

	/**
	 * Pushes the project operator into each subplan under the union operator.
	 *
	 * The heuristic is then applied recursively to each subplan.
	 * Finally, all rewritten subplans are collected under a multiway union
	 * as the new root operator.
	 */
	protected LogicalPlan createPlanForUnionUnderProject( final LogicalOpProject projectOp,
	                                                      final LogicalPlan subPlanUnderProject ) {
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

	/**
	 * Attempts to push a project operator below a bind operator.
	 *
	 * The pushed projection is adjusted to ensure correctness:
	 * <ul>
	 * 	<li> Variables assigned by the bind operator are removed, since they
	 *       are produced by the bind and not needed as input.</li>
	 * 	<li> Variables required to evaluate the bind expressions are added,
	 *       to ensure the bind can still be evaluated after pushdown.</li>
	 * </ul>
	 *
	 * If this adjustment does not change the projection, the whole project
	 * can be pushed below the bind.
	 *
	 * If the adjusted projection contains all the variables of the subplan
	 * under the bind (including both the certain and the possible variables),
	 * then there is no need to push the adjusted projection at all and the
	 * plan can remain as is.
	 *
	 * Otherwise, the project is split: the adjusted projection is pushed below
	 * the bind, while the original projection remains on top.
	 */
	protected LogicalPlan createPlanForBindUnderProject( final LogicalOpProject projectOp,
	                                                     final LogicalOpBind bindOp,
	                                                     final LogicalPlan subPlanUnderBind,
	                                                     final LogicalPlan inputPlan ) {
		// Determine the projection variables to be pushed, starting with all the projection variables.
		final Set<Var> pushedProjectVars = new HashSet<>( projectOp.getVariables() );

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
		if ( pushedProjectVars.containsAll(subPlanUnderBind.getExpectedVariables().getCertainVariables())
		  && pushedProjectVars.containsAll(subPlanUnderBind.getExpectedVariables().getPossibleVariables()) ) {
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

	/**
	 * Attempts to push a project operator below an unfold operator.
	 *
	 * The pushed projection is adjusted similarly to the bind case:
	 * <ul>
	 * 	<li> Variables assigned by the unfold operator are removed, since
	 *       they are produced by the unfold.</li>
	 * 	<li> Variables required to evaluate the unfold expression are added,
	 *       to ensure the unfold can still be evaluated after pushdown.</li>
	 * </ul>
	 *
	 * If this adjustment does not change the projection, the whole project
	 * can be pushed below the unfold.
	 *
	 * If the adjusted projection contains all the variables of the subplan
	 * under the unfold (including both the certain and the possible variables),
	 * then there is no need to push the adjusted projection at all and the
	 * plan can remain as is.
	 *
	 * Otherwise, the project is split: the adjusted projection is pushed below
	 * the unfold, while the original projection remains on top.
	 */
	protected LogicalPlan createPlanForUnfoldUnderProject(
			final LogicalOpProject projectOp,
			final LogicalOpUnfold unfoldOp,
			final LogicalPlan subPlanUnderUnfold,
			final LogicalPlan inputPlan ) {
		// Determine the projection variables to be pushed, starting with all the projection variables.
		final Set<Var> pushedProjectVars = new HashSet<>( projectOp.getVariables() );

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

		// If adjusted projection contains all variables of the subplan under the unfold operator,
		// return the plan as is
		if ( pushedProjectVars.containsAll(subPlanUnderUnfold.getExpectedVariables().getCertainVariables())
		  && pushedProjectVars.containsAll(subPlanUnderUnfold.getExpectedVariables().getPossibleVariables()) ) {
			return inputPlan;
		}

		// Otherwise, split
		final LogicalPlan pushed = new LogicalPlanWithUnaryRootImpl(
			new LogicalOpProject(pushedProjectVars, projectOp.mayReduce()),
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
	 *
	 * While pushing the project operator under an l2g or a g2l operator is safe in
	 * some cases, it is not generally safe.
	 *
	 * Both operators may:
	 * <ul>
	 * 	<li>map one input solution mapping to multiple output solution mappings</li>
	 * 	<li>drop solution mappings entirely in some cases</li>
	 * </ul>
	 *
	 * Pushing projection can therefore:
	 * <ul>
	 * 	<li>change duplication behaviour of results</li>
	 * 	<li>incorrectly prevent solution mappings from being dropped</li>
	 * </ul>
	 *
	 * TODO: think more about cases in which pushing a project operator is safe.
	 */
	protected LogicalPlan createPlanForL2GOrG2LUnderProject( final LogicalOpProject projectOp,
	                                                        final UnaryLogicalOp childOp,
	                                                        final LogicalPlan subPlanUnderChildOp ) {
		return apply(subPlanUnderChildOp);
	}

	/**
	 * Pushes a project operator below a gpAdd or gpOptAdd operator.
	 *
	 * The pushdown must preserve:
	 * <ul>
	 * 	<li>variables required by the parent projection</li>
	 * 	<li>variables required by the operator (e.g., parameter variables or optional bindings)</li>
	 * 	<li>join variables between the subplan and the operator pattern</li>
	 * </ul>
	 *
	 * If all projected variables are guaranteed by the subplan and include all required variables,
	 * the projection is fully pushed below the operator.
	 * Otherwise, a reduced projection is pushed down while the original projection is retained above.
	 *
	 * If no beneficial pushdown is possible, the operator is left unchanged, but the heuristic
	 * is still applied recursively to the subplan.
	 */
	protected LogicalPlan createPlanForAddOpUnderProject( final LogicalOpProject parentProjectOp,
	                                                      final UnaryLogicalOp childOp,
	                                                      final LogicalPlan subPlanUnderChildOp,
	                                                      final LogicalPlan inputPlan ) {
		final ExpectedVariables expVarsInSubPlan = subPlanUnderChildOp.getExpectedVariables();

		final ExpectedVariables expVarsInPattern;
		if ( childOp instanceof LogicalOpGPAdd gpAdd ) {
			expVarsInPattern = gpAdd.getPattern().getExpectedVariables();
		}
		else {
			final LogicalOpGPOptAdd op = (LogicalOpGPOptAdd) childOp;
			expVarsInPattern = op.getPattern().getExpectedVariables();
		}

		final Set<Var> joinVars = ExpectedVariablesUtils.intersectionOfAllVariables(
				expVarsInPattern,
				expVarsInSubPlan );

		// Case 1:
		// We can push the full projection below the gpAdd/gpOptAdd operator
		// only if:
		//	(1) the subplan guarantees all projected variables,
		//	(2) the operator does not require any extra variables
		//	  (e.g., parameter variables would be lost by projection), and
		//	(3) the projected variables include all join variables
		if ( expVarsInSubPlan.getCertainVariables().containsAll(parentProjectOp.getVariables())
		  && operatorDoesNotRequireExtraVars(childOp, parentProjectOp)
		  && parentProjectOp.getVariables().containsAll(joinVars) ) {
			final LogicalPlan pushed = new LogicalPlanWithUnaryRootImpl(parentProjectOp, null, subPlanUnderChildOp);

			return new LogicalPlanWithUnaryRootImpl(childOp, null, apply(pushed));
		}

		// Case 2:
		// We attempt a partial pushdown of the projection.
		// We push down only:
		//	(1) variables from the projection that are needed by the subplan output
		//	(2) variables required by the operator (e.g., gpAdd parameters, optional bindings)
		//	(3) join variables
		//
		// The remaining projection is kept above the operator.
		final Set<Var> varsInSubPlan = ExpectedVariablesUtils.unionOfAllVariables(expVarsInSubPlan);
		final Set<Var> neededByProject = new HashSet<>(parentProjectOp.getVariables());
		neededByProject.retainAll( varsInSubPlan );

		final Set<Var> neededByOperator = new HashSet<>();
		if ( childOp instanceof LogicalOpGPAdd gpAdd && gpAdd.hasParameterVariables() ) {
			// gpAdd parameter variables must be preserved by the projection.
			// Ensure they are included in the pushed projection.
			final Collection<Var> paramVars = gpAdd.getParameterVariables().values();
			neededByOperator.addAll( paramVars );
		}
		neededByOperator.retainAll( varsInSubPlan );

		final Set<Var> pushDownVars = new HashSet<>();
		pushDownVars.addAll( neededByProject );
		pushDownVars.addAll( neededByOperator );
		pushDownVars.addAll( joinVars );

		if ( pushDownVars.isEmpty() || pushDownVars.equals(varsInSubPlan) ) {
			// No projection needed for this branch
			final LogicalPlan newChild = apply(subPlanUnderChildOp);

			if ( newChild.isSamePlan(subPlanUnderChildOp) )
				return inputPlan;

			final LogicalPlan newSubTree = new LogicalPlanWithUnaryRootImpl(
				childOp,
				null,
				newChild );

			return new LogicalPlanWithUnaryRootImpl(parentProjectOp, null, newSubTree);
		}

		final LogicalPlan pushed = new LogicalPlanWithUnaryRootImpl(
			new LogicalOpProject(pushDownVars, parentProjectOp.mayReduce()),
			null,
			subPlanUnderChildOp );

		final LogicalPlan rewrittenPushed = apply(pushed);

		final LogicalPlan newChild = new LogicalPlanWithUnaryRootImpl(
			childOp,
			null,
			rewrittenPushed );

		return new LogicalPlanWithUnaryRootImpl(parentProjectOp, null, newChild);
	}

	/**
	 * Pushes a projection below a n-ary operator by splitting required variables per branch.
	 *
	 * Each branch must retain:
	 * <ul>
	 *	<li>its subset of the variables of the parent projection</li>
	 *	<li>its subset of the join variables</li>
	 * </ul>
	 */
	protected LogicalPlan createPlanForJoinLikeOpUnderProject( final LogicalOpProject projectOp,
	                                                       final LogicalPlan subPlanUnderProject,
	                                                       final LogicalPlan inputPlan ) {
		final int numberOfSubPlansUnderJoin = subPlanUnderProject.numberOfSubPlans();

		// If there is only one subplan under the join, then remove the join altogether.
		if ( numberOfSubPlansUnderJoin == 1 ) {
			final LogicalPlan planWithProject = new LogicalPlanWithUnaryRootImpl(
				projectOp,
				null,
				subPlanUnderProject.getSubPlan(0) );
			return apply(planWithProject);
		}

		final Set<Var> joinVars = computeJoinVars(subPlanUnderProject);
		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlansUnderJoin];

		// For each subplan under the join, compute which variables must be preserved locally:
		// - variables required by the top-level projection
		// - variables required to preserve join correctness in this branch
		//
		// The union of these variables is pushed down as a projection into the branch.
		boolean noChanges = true; // set to false if something has changed in the plan
		final Set<Var> allPushdownVars = new HashSet<>();
		for (int i = 0; i < numberOfSubPlansUnderJoin; i++) {
			final LogicalPlan subPlan = subPlanUnderProject.getSubPlan(i);
			final Set<Var> varsInSubPlan = ExpectedVariablesUtils.unionOfAllVariables( subPlan.getExpectedVariables() );

			final Set<Var> pushDownVars = new HashSet<>();
			pushDownVars.addAll( projectOp.getVariables() );
			pushDownVars.addAll( joinVars );
			pushDownVars.retainAll( varsInSubPlan );

			allPushdownVars.addAll( pushDownVars );

			if ( pushDownVars.isEmpty() || pushDownVars.equals(varsInSubPlan) ) {
				// No projection needed for this branch
				newSubPlans[i] = apply(subPlan);
			}
			else {
				final LogicalOpProject branchProject = new LogicalOpProject(pushDownVars, projectOp.mayReduce());
				final LogicalPlan withProject = new LogicalPlanWithUnaryRootImpl(branchProject, null, subPlan);

				newSubPlans[i] = apply(withProject);
			}

			if ( !newSubPlans[i].isSamePlan(subPlan) )
				noChanges = false;
		}

		if ( noChanges )
			return inputPlan;

		final LogicalOperator joinOp = subPlanUnderProject.getRootOperator();

		final LogicalPlan newJoin = LogicalPlanUtils.createPlanWithSubPlans(
				joinOp,
				null,
				newSubPlans );

		// If the top project is redundant, drop it
		if ( projectOp.getVariables().equals(allPushdownVars) ) {
			return newJoin;
		}

		// Otherwise, keep original project on top
		return new LogicalPlanWithUnaryRootImpl(projectOp, null, newJoin);
	}

	/**
	 * Merges a parent and child project operator into a single project operator.
	 *
	 * The resulting projection contains the intersection of the variables from
	 * both operators and is applied to the subplan under the child project.
	 */
	protected LogicalPlan createPlanForProjectUnderProject( final LogicalOpProject parentProjectOp,
	                                                        final LogicalOpProject childProjectOp,
	                                                        final LogicalPlan subPlanUnderChildProjectOp ) {
		final Set<Var> intersectionOfVars = new HashSet<>(childProjectOp.getVariables());
		intersectionOfVars.retainAll( parentProjectOp.getVariables() );

		final LogicalPlan newPlan = new LogicalPlanWithUnaryRootImpl(
			new LogicalOpProject(intersectionOfVars, parentProjectOp.mayReduce()),
			null,
			subPlanUnderChildProjectOp );

		return apply(newPlan);
	}

	/**
	 * Returns a plan in which the given project operator is pushed under the
	 * given unary operator, with the subplan underneath being a version of
	 * the given subplan in which the project push down heuristic has been
	 * applied recursively.
	 */
	protected LogicalPlan createPlanForUnaryOpUnderProject( final LogicalOpProject projectOp,
	                                                        final UnaryLogicalOp op,
	                                                        final LogicalPlan subPlanUnderOp ) {
		final LogicalPlan newSubPlan1 = LogicalPlanUtils.createPlanWithSubPlans(
				projectOp,
				null,
				subPlanUnderOp );
		final LogicalPlan newSubPlan2 = apply(newSubPlan1);

		// Put together the new plan with the given unary operator as root.
		return LogicalPlanUtils.createPlanWithSubPlans(op, null, newSubPlan2);
	}



	/**
	 * Determines the set of all join variables between the subplans of the
	 * given plan, where a join variable is a variable (certain or possible)
	 * that occurs in at least two subplan.
	 */
	protected Set<Var> computeJoinVars( final LogicalPlan joinPlan ) {
		final Set<Var> seen = new HashSet<>();
		final Set<Var> joinVars = new HashSet<>();

		for ( int i = 0; i < joinPlan.numberOfSubPlans(); i++ ) {
			final LogicalPlan sp = joinPlan.getSubPlan(i);

			// Certain variables
			for ( final Var v : sp.getExpectedVariables().getCertainVariables() ) {
				if ( ! seen.add(v) )
					joinVars.add(v);
			}

			// Possible variables
			for ( final Var v : sp.getExpectedVariables().getPossibleVariables() ) {
				if ( ! seen.add(v) )
					joinVars.add(v);
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