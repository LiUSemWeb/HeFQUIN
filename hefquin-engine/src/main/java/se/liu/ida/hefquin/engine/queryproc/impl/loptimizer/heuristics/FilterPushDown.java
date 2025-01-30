package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVars;

import se.liu.ida.hefquin.base.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

/**
 * Pushes filter conditions as much as possible towards the leaf nodes
 * of a given logical plan. Whether a particular filter condition can
 * be pushed, and into which child plan(s) it is pushed depends on the
 * variables that it mentions.
 *
 * For cases in which a filter operator has a request operator as its
 * child plan and this request operator is for a federation member that
 * is capable of handling requests that contain filter conditions, such
 * as a SPARQL endpoint, the filter is merged into the request.
 *
 * Conjunctive filter conditions (i.e., conditions that consist of multiple
 * sub-conditions that are connected via a logical AND) are split up into
 * separate conditions for each of the conjuncts, which may make it possible
 * to push some of the conjuncts even if the whole conjunctive filter
 * condition cannot be pushed.
 */
public class FilterPushDown implements HeuristicForLogicalOptimization
{
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if ( rootOp instanceof LogicalOpFilter ) {
			return applyToPlanWithFilterAsRootOperator( (LogicalOpFilter) rootOp,
			                                            inputPlan.getSubPlan(0), // subPlanUnderFilter
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
			else
				return LogicalPlanUtils.createPlanWithSubPlans(rootOp, newSubPlans);
		}
	}

	protected LogicalPlan applyToPlanWithFilterAsRootOperator( final LogicalOpFilter filterOp,
	                                                           final LogicalPlan subPlanUnderFilter,
	                                                           final LogicalPlan inputPlan ) {
		final LogicalOperator childOpUnderFilter = subPlanUnderFilter.getRootOperator();
		if ( childOpUnderFilter instanceof LogicalOpRequest requestOpUnderFilter )
		{
			return createPlanForRequestUnderFilter( filterOp,
			                                        requestOpUnderFilter,
			                                        inputPlan );
		}
		else if ( childOpUnderFilter instanceof LogicalOpFilter filterOpUnderFilter )
		{
			return createPlanForFilterUnderFilter( filterOp,
			                                       filterOpUnderFilter,
			                                       subPlanUnderFilter.getSubPlan(0) );
		}
		else if ( childOpUnderFilter instanceof LogicalOpBind bindOpUnderFilter )
		{
			return createPlanForBindUnderFilter( filterOp,
			                                     bindOpUnderFilter,
			                                     subPlanUnderFilter.getSubPlan(0),
			                                     inputPlan );
		}
		else if (    childOpUnderFilter instanceof LogicalOpLocalToGlobal
		          || childOpUnderFilter instanceof LogicalOpGlobalToLocal )
		{
			return createPlanForL2GOrG2LUnderFilter( filterOp,
			                                         (UnaryLogicalOp) childOpUnderFilter,
			                                         subPlanUnderFilter.getSubPlan(0),
			                                         inputPlan );
		}
		else if (    childOpUnderFilter instanceof LogicalOpTPAdd
		          || childOpUnderFilter instanceof LogicalOpTPOptAdd
		          || childOpUnderFilter instanceof LogicalOpBGPAdd
		          || childOpUnderFilter instanceof LogicalOpBGPOptAdd
		          || childOpUnderFilter instanceof LogicalOpGPAdd
		          || childOpUnderFilter instanceof LogicalOpGPOptAdd )
		{
			return createPlanForAddOpUnderFilter( filterOp,
			                                      (UnaryLogicalOp) childOpUnderFilter,
			                                      subPlanUnderFilter.getSubPlan(0),
			                                      inputPlan );
		}
		else if (    childOpUnderFilter instanceof LogicalOpUnion
		          || childOpUnderFilter instanceof LogicalOpMultiwayUnion )
		{
			return createPlanForUnionUnderFilter(filterOp, subPlanUnderFilter);
		}
		else if (    childOpUnderFilter instanceof LogicalOpJoin
		          || childOpUnderFilter instanceof LogicalOpMultiwayJoin )
		{
			return createPlanForJoinUnderFilter(filterOp, subPlanUnderFilter, inputPlan);
		}
		else if ( childOpUnderFilter instanceof LogicalOpRightJoin )
		{
			return createPlanForRightJoinUnderFilter( filterOp,
			                                          subPlanUnderFilter.getSubPlan(1), // non-optional subplan
			                                          subPlanUnderFilter.getSubPlan(0), // optional subplan
			                                          inputPlan );
		}
		else if ( childOpUnderFilter instanceof LogicalOpMultiwayLeftJoin )
		{
			return createPlanForMultiwayLeftJoinUnderFilter(filterOp, subPlanUnderFilter, inputPlan);
		}
		else
		{
			throw new IllegalArgumentException( childOpUnderFilter.getClass().getName() );
		}
	}

	protected LogicalPlan createPlanForRequestUnderFilter( final LogicalOpFilter filterOp,
	                                                       final LogicalOpRequest<?,?> reqOp,
	                                                       final LogicalPlan inputPlan ) {
		if ( reqOp.getFederationMember().getInterface().supportsSPARQLPatternRequests() ) {
			final SPARQLEndpoint ep = (SPARQLEndpoint) reqOp.getFederationMember();
			final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();
			return MergeRequests.mergeFilterIntoSPARQLEndpointRequest(filterOp, ep, req);
		}
		else {
			return inputPlan;
		}
	}

	protected LogicalPlan createPlanForFilterUnderFilter( final LogicalOpFilter parentFilterOp,
	                                                      final LogicalOpFilter childFilterOp,
	                                                      final LogicalPlan subPlanUnderChildFilterOp ) {
		final ExprList combinedFilterExprsWithoutAND = new ExprList();

		final ExprList parentFilterExprs = parentFilterOp.getFilterExpressions();
		final ExprList parentFilterExprsWithoutAND = splitConjunctions(parentFilterExprs);
		combinedFilterExprsWithoutAND.addAll(parentFilterExprsWithoutAND);

		final ExprList childFilterExprs = childFilterOp.getFilterExpressions();
		final ExprList childFilterExprsWithoutAND = splitConjunctions(childFilterExprs);
		combinedFilterExprsWithoutAND.addAll(childFilterExprsWithoutAND);

		final LogicalOpFilter newFilterOp = new LogicalOpFilter(combinedFilterExprsWithoutAND);
		final LogicalPlan newPlan = new LogicalPlanWithUnaryRootImpl(newFilterOp, subPlanUnderChildFilterOp);

		return apply(newPlan);
	}

	protected LogicalPlan createPlanForUnionUnderFilter( final LogicalOpFilter filterOp,
	                                                     final LogicalPlan subPlanUnderFilter ) {
		// simply pushes the filter operator into every subplan under the
		// union operator and, then, applies this heuristic recursively to
		// each such subplan; in the end, collects all resulting subplans
		// again under a multiway union as new root operator
		final int numberOfSubPlansUnderUnion = subPlanUnderFilter.numberOfSubPlans();
		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlansUnderUnion];
		for ( int i = 0; i < numberOfSubPlansUnderUnion; i++ ) {
			final LogicalPlan subPlanUnderUnion = subPlanUnderFilter.getSubPlan(i);
			final LogicalPlan newSubPlanWithFilterAsRoot = new LogicalPlanWithUnaryRootImpl(filterOp, subPlanUnderUnion);
			final LogicalPlan newSubPlanWithFilterPushed = apply(newSubPlanWithFilterAsRoot);
			newSubPlans[i] = newSubPlanWithFilterPushed;
		}

		// If there is only one subplan under the union, then remove the union altogether.
		if ( numberOfSubPlansUnderUnion == 1 ) return newSubPlans[0];

		final LogicalOperator unionOp = subPlanUnderFilter.getRootOperator(); // may be multiway union or binary union
		return LogicalPlanUtils.createPlanWithSubPlans(unionOp, newSubPlans);
	}

	protected LogicalPlan createPlanForBindUnderFilter( final LogicalOpFilter filterOp,
	                                                    final LogicalOpBind bindOp,
	                                                    final LogicalPlan subPlanUnderBind,
	                                                    final LogicalPlan inputPlan ) {
		// Check whether the filter can be pushed under the given bind operator,
		// which is possible only if none of the variables assigned by the bind
		// operator is used in the filter condition.
		final Set<Var> varsInFilter = ExprVars.getVarsMentioned( filterOp.getFilterExpressions() );
		final List<Var> varsInBind = bindOp.getBindExpressions().getVars();
		if ( ! Collections.disjoint(varsInFilter, varsInBind) )
			return inputPlan;

		// The filter can be pushed. In this case, create a new subplan with
		// the filter as root operator on top of the subplan that was under
		// the bind, and apply this heuristic recursively to this new subplan.
		final LogicalPlan newSubPlan1 = LogicalPlanUtils.createPlanWithSubPlans(filterOp, subPlanUnderBind);
		final LogicalPlan newSubPlan2 = apply(newSubPlan1);

		// Finally, put together the new plan with the bind operator as root.
		return LogicalPlanUtils.createPlanWithSubPlans(bindOp, newSubPlan2);
	}

	/**
	 * Assumes that the given child operator is either a {@link LogicalOpLocalToGlobal}
	 * or a {@link LogicalOpGlobalToLocal}.
	 */
	protected LogicalPlan createPlanForL2GOrG2LUnderFilter( final LogicalOpFilter parentFilterOp,
	                                                        final UnaryLogicalOp childOp,
	                                                        final LogicalPlan subPlanUnderChildOp,
	                                                        final LogicalPlan inputPlan ) {
		// The current implementation does not try to push filter
		// conditions under the vocabulary rewriting operators,
		// LogicalOpLocalToGlobal and LogicalOpGlobalToLocal.
		// Extending the implementation to try to push filter
		// conditions in these cases is captured as issue #271.
		//
		// https://github.com/LiUSemWeb/HeFQUIN/issues/271
		//
		// However, for the time being, we only apply the filter
		// push-down heuristic to the subplan that is under the
		// given vocabulary rewriting operator.
		return createPlanAfterPushingInSubPlan(parentFilterOp, childOp, subPlanUnderChildOp, inputPlan);
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
	protected LogicalPlan createPlanForAddOpUnderFilter( final LogicalOpFilter parentFilterOp,
	                                                     final UnaryLogicalOp childOp,
	                                                     final LogicalPlan subPlanUnderChildOp,
	                                                     final LogicalPlan inputPlan ) {
		// The following snippets of code will be needed if we try to push
		// the filter conditions also into the pattern of the xxAdd operator.
		// Trying to do so is not necessary at the moment because we do not
		// actually have xxAdd operators in the logical plans to which we
		// apply the heuristics (because the initial logical plan is always
		// a source assignment and we currently don't have any heuristics
		// that rewrite the multiway joins of such source assignments into
		// xxAdd operators). Even once we have heuristics that rewrite joins
		// into xxAdd operators, as long as we apply the MergeRequest heuristic
		// before such a heuristic (and after FilterPushDown), we would never
		// need to try pushing filter conditions also into the pattern of the
		// xxAdd operator.
		// However, should we ever try to push the filter conditions also
		// into the pattern of the xxAdd operator, notice that the xxOptAdd
		// operators are special because not every filter condition can be
		// pushed into the pattern of such an operator even if the certain
		// variables of that pattern contain all of the variables mention
		// in the filter condition. For instance, if the optional pattern
		// has ?x as a certain variable and the filter condition is
		// !BOUND(?x), then pushing this condition is not possible.

		//final SPARQLGraphPattern patternOfAddOp;
		//if      ( childOp instanceof LogicalOpTPAdd )     patternOfAddOp = ((LogicalOpTPAdd) childOp).getTP();
		//else if ( childOp instanceof LogicalOpTPOptAdd )  patternOfAddOp = ((LogicalOpTPOptAdd) childOp).getTP();
		//else if ( childOp instanceof LogicalOpBGPAdd )    patternOfAddOp = ((LogicalOpBGPAdd) childOp).getBGP();
		//else if ( childOp instanceof LogicalOpBGPOptAdd ) patternOfAddOp = ((LogicalOpBGPOptAdd) childOp).getBGP();
		//else if ( childOp instanceof LogicalOpGPAdd )     patternOfAddOp = ((LogicalOpGPAdd) childOp).getPattern();
		//else if ( childOp instanceof LogicalOpGPOptAdd )  patternOfAddOp = ((LogicalOpGPOptAdd) childOp).getPattern();
		//else throw new IllegalArgumentException( childOp.getClass().getName() );

		//final ExpectedVariables expVarsInPattern = QueryPatternUtils.getExpectedVariablesInPattern(patternOfAddOp);
		//final Set<Var> certainVarsInPattern = expVarsInPattern.getCertainVariables();

		final ExpectedVariables expVarsInSubPlan = subPlanUnderChildOp.getExpectedVariables();
		final Set<Var> certainVarsInSubPlan = expVarsInSubPlan.getCertainVariables();

		final ExprList filterExprs = parentFilterOp.getFilterExpressions();
		final ExprList filterExprsWithoutAND = splitConjunctions(filterExprs);

		final ExprList toBePushed = new ExprList();
		final ExprList toBeKept = new ExprList();

		for ( final Expr e : filterExprsWithoutAND ) {
			// If all of the variables mentioned in the current filter
			// condition are certain variables of the subplan, then the
			// condition can be pushed to the subplan.
			final Set<Var> varsInExpr = ExprVars.getVarsMentioned(e);
			if ( certainVarsInSubPlan.containsAll(varsInExpr) )
				toBePushed.add(e);
			else
				toBeKept.add(e);
		}

		if ( toBePushed.isEmpty() ) {
			return createPlanAfterPushingInSubPlan(parentFilterOp, childOp, subPlanUnderChildOp, inputPlan);
		}

		final LogicalOpFilter pushedFilterOp = new LogicalOpFilter(toBePushed);
		final LogicalPlan newSubPlanUnderChildOp = new LogicalPlanWithUnaryRootImpl(pushedFilterOp, subPlanUnderChildOp);
		final LogicalPlan newSubPlanUnderChildOpRewritten = apply(newSubPlanUnderChildOp);

		final LogicalPlan newSubPlanUnderRootOp = new LogicalPlanWithUnaryRootImpl(childOp, newSubPlanUnderChildOpRewritten);

		if ( toBeKept.isEmpty() ) {
			return newSubPlanUnderRootOp;
		}

		final LogicalOpFilter newRootFilterOp = new LogicalOpFilter(toBeKept);
		return new LogicalPlanWithUnaryRootImpl(newRootFilterOp, newSubPlanUnderRootOp);
	}

	protected LogicalPlan createPlanForJoinUnderFilter( final LogicalOpFilter filterOp,
	                                                    final LogicalPlan subPlanUnderFilter,
	                                                    final LogicalPlan inputPlan ) {
		final int numberOfSubPlansUnderJoin = subPlanUnderFilter.numberOfSubPlans();

		// If there is only one subplan under the join, then remove the join altogether.
		if ( numberOfSubPlansUnderJoin == 1 ) {
			final LogicalPlan subPlan = subPlanUnderFilter.getSubPlan(0);
			final LogicalPlan subPlanAfterFilterPushDown = apply(subPlan);
			return new LogicalPlanWithUnaryRootImpl(filterOp, subPlanAfterFilterPushDown);
		}

		// Determine the sets of certain variables in each of the subplans
		// under the join, which will be needed later for checking which of
		// the filter conditions may be pushed to the subplans.
		final List<Set<Var>> certainVarsInSubPlans = new ArrayList<>(numberOfSubPlansUnderJoin);
		for ( int i = 0; i < numberOfSubPlansUnderJoin; i++ ) {
			final LogicalPlan subPlanUnderJoin = subPlanUnderFilter.getSubPlan(i);
			final ExpectedVariables expVarsInSubPlan = subPlanUnderJoin.getExpectedVariables();
			certainVarsInSubPlans.add( expVarsInSubPlan.getCertainVariables() );
		}

		final ExprList filterExprs = filterOp.getFilterExpressions();
		final ExprList filterExprsWithoutAND = splitConjunctions(filterExprs);

		// Determine which of the filter conditions may be pushed
		// to the subplans under the join and which ones need to
		// be kept to be evaluated after the join.
		final ExprList[] toBePushed = new ExprList[numberOfSubPlansUnderJoin];
		ExprList toBeKept = null;

		for ( final Expr e : filterExprsWithoutAND ) {
			boolean pushed = false; // set to true if the current condition can be pushed to at least one of the subplans
			final Set<Var> varsInExpr = ExprVars.getVarsMentioned(e);

			// check the current filter condition for each of the subplans
			for ( int i = 0; i < numberOfSubPlansUnderJoin; i++ ) {
				// If all of the variables mentioned in the current filter
				// condition are certain variables of the current subplan,
				// then the condition can be pushed to the subplan.
				if ( certainVarsInSubPlans.get(i).containsAll(varsInExpr) ) {
					pushed = true;
					if ( toBePushed[i] == null )
						toBePushed[i] = new ExprList(e);
					else
						toBePushed[i].add(e);
				}
			}

			// If the current condition cannot be pushed to any of the
			// subplans, then it needs to be kept for the filter that
			// will be evaluated after the join.
			if ( ! pushed ) {
				if ( toBeKept == null )
					toBeKept = new ExprList(e);
				else
					toBeKept.add(e);
			}
		}

		// Now set up the new subplans of the join as well as
		// the filter operator to be executed after the join.
		final LogicalPlan[] newSubPlansUnderJoin = new LogicalPlan[numberOfSubPlansUnderJoin];
		final LogicalOpFilter newFilterOp;

		// ... to this end, we need to consider two cases: either all filter
		// conditions need to be kept in the filter operator after the join
		// or some of them can be pushed to some subplan(s).
		if ( toBeKept != null && toBeKept.size() == filterExprsWithoutAND.size() ) {
			// If all filter conditions need to be kept in the filter operator after
			// the join, then we can simply continue using that filter operator, ...
			newFilterOp = filterOp;

			// ... and we apply this heuristic recursively to each of the subplans.
			boolean noChanges = true;
			for ( int i = 0; i < numberOfSubPlansUnderJoin; i++ ) {
				final LogicalPlan oldSubPlan = subPlanUnderFilter.getSubPlan(i);
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

			// If none of the subplans was changed by the heuristic, we can
			// simply return the given input plan (recall that we are in the
			// case where also none of the filter conditions can be pushed).
			if ( noChanges ) return inputPlan;
		}
		else {
			// If some of the filter conditions can be pushed to some
			// subplan(s), create a new filter operator with only the
			// conditions to be kept for evaluation after the join, ...
			if ( toBeKept == null )
				newFilterOp = null;
			else
				newFilterOp = new LogicalOpFilter(toBeKept);

			// ... and iterate over the subplans to push their respective
			// filter conditions to them (if any) and apply the heuristic
			// recursively to the resulting subplans.
			for ( int i = 0; i < numberOfSubPlansUnderJoin; i++ ) {
				final LogicalPlan oldSubPlan = subPlanUnderFilter.getSubPlan(i);
				if ( toBePushed[i] == null ) {
					// If there are no filter conditions to be pushed to the
					// current subplan, then only apply the heuristic to it.
					final LogicalPlan newSubPlan = apply(oldSubPlan);
					newSubPlansUnderJoin[i] = (newSubPlan.equals(oldSubPlan)) ? oldSubPlan : newSubPlan;
				}
				else {
					// There are filter conditions to be pushed to the current subplan.
					final LogicalOpFilter filterForSubPlan = new LogicalOpFilter( toBePushed[i] );
					final LogicalPlan newSubPlanWithFilterAsRoot = new LogicalPlanWithUnaryRootImpl(filterForSubPlan, oldSubPlan);
					final LogicalPlan newSubPlanWithFilterPushed = apply(newSubPlanWithFilterAsRoot);
					newSubPlansUnderJoin[i] = newSubPlanWithFilterPushed;
				}
			}
		}

		// Create the new rewritten plan to be returned.
		final LogicalOperator joinOp = subPlanUnderFilter.getRootOperator(); // may be multiway join or binary join
		final LogicalPlan newSubPlanUnderFilter = LogicalPlanUtils.createPlanWithSubPlans( joinOp,
		                                                                                   newSubPlansUnderJoin );
		if ( newFilterOp == null )
			return newSubPlanUnderFilter;
		else
			return new LogicalPlanWithUnaryRootImpl(newFilterOp, newSubPlanUnderFilter);
	}

	protected LogicalPlan createPlanForRightJoinUnderFilter( final LogicalOpFilter filterOp,
	                                                         final LogicalPlan nonoptSubPlan,
	                                                         final LogicalPlan optSubPlan,
	                                                         final LogicalPlan inputPlan ) {
		// Note that filter conditions may not generally be pushed into the
		// optional subplans even for cases in which all variables mentioned
		// in a filter condition are certain variables of an optional subplan.
		// As a counter example, consider the filter condition !BOUND(?x) and
		// an optional subplan that has ?x as a certain variable. Then, the
		// condition cannot be pushed into this subplan.

		final ExprList filterExprs = filterOp.getFilterExpressions();
		final ExprList filterExprsWithoutAND = splitConjunctions(filterExprs);

		// Create the new non-optional subplan. 
		final ExprList toBeKept = new ExprList(); // will be populated by 'pushToNonOptSubPlan'
		final LogicalPlan newNonOptSubPlan = pushToNonOptSubPlan( filterExprsWithoutAND,
		                                                          nonoptSubPlan,
		                                                          toBeKept );

		// Now set up the new optional subplan of the join by
		// applying this heuristic recursively to it.
		final LogicalPlan newOptSubPlan = apply(optSubPlan);

		// Now set up the filter operator to be executed after the join.
		final LogicalOpFilter newFilterOp;
		if ( toBeKept.size() == filterExprsWithoutAND.size() ) {
			// If all filter conditions need to be kept in the filter operator
			// after the join, then we can simply continue using that operator.
			newFilterOp = filterOp;
		}
		else {
			// If some filter conditions have been pushed to the non-optional
			// subplan, create a new filter operator with only the conditions
			// to be kept for evaluation after the join.
			if ( toBeKept.isEmpty() )
				newFilterOp = null;
			else
				newFilterOp = new LogicalOpFilter(toBeKept);
		}

		// If nothing was changed, return the given input plan.
		if (    newFilterOp == filterOp
		     && newNonOptSubPlan.equals(nonoptSubPlan)
		     && newOptSubPlan.equals(optSubPlan) ) {
			return inputPlan;
		}

		// Create the new rewritten plan to be returned.
		final LogicalPlan newSubPlanUnderFilter = new LogicalPlanWithBinaryRootImpl( LogicalOpRightJoin.getInstance(),
		                                                                             newOptSubPlan,
		                                                                             newNonOptSubPlan );
		if ( newFilterOp == null )
			return newSubPlanUnderFilter;
		else
			return new LogicalPlanWithUnaryRootImpl(newFilterOp, newSubPlanUnderFilter);
	}

	protected LogicalPlan createPlanForMultiwayLeftJoinUnderFilter( final LogicalOpFilter filterOp,
	                                                                final LogicalPlan subPlanUnderFilter,
	                                                                final LogicalPlan inputPlan ) {
		// Note that filter conditions may not generally be pushed into the
		// optional subplans even for cases in which all variables mentioned
		// in a filter condition are certain variables of an optional subplan.
		// As a counter example, consider the filter condition !BOUND(?x) and
		// an optional subplan that has ?x as a certain variable. Then, the
		// condition cannot be pushed into this subplan.

		final int numberOfSubPlansUnderJoin = subPlanUnderFilter.numberOfSubPlans();
		final LogicalPlan[] newSubPlansUnderJoin = new LogicalPlan[numberOfSubPlansUnderJoin];

		final ExprList filterExprs = filterOp.getFilterExpressions();
		final ExprList filterExprsWithoutAND = splitConjunctions(filterExprs);

		// Create the new non-optional subplan. 
		final ExprList toBeKept = new ExprList(); // will be populated by 'pushToNonOptSubPlan'
		final LogicalPlan oldNonOptSubPlan = subPlanUnderFilter.getSubPlan(0);
		newSubPlansUnderJoin[0] = pushToNonOptSubPlan( filterExprsWithoutAND,
		                                               oldNonOptSubPlan,
		                                               toBeKept );

		// Now set up the new optional subplans of the join by
		// applying this heuristic recursively to each of them.
		boolean noChanges = true; // set to false if something has changed in the plan
		for ( int i = 1; i < numberOfSubPlansUnderJoin; i++ ) {
			final LogicalPlan oldSubPlan = subPlanUnderFilter.getSubPlan(i);
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

		// Now set up the filter operator to be executed after the join.
		final LogicalOpFilter newFilterOp;
		if ( toBeKept.size() == filterExprsWithoutAND.size() ) {
			// If all filter conditions need to be kept in the filter operator
			// after the join, then we can simply continue using that operator.
			newFilterOp = filterOp;
		}
		else {
			// If some filter conditions have been pushed to the non-optional
			// subplan, create a new filter operator with only the conditions
			// to be kept for evaluation after the join.
			if ( toBeKept.isEmpty() )
				newFilterOp = null;
			else
				newFilterOp = new LogicalOpFilter(toBeKept);

			noChanges = false;
		}

		// If nothing was changed, return the given input plan.
		if ( noChanges && newSubPlansUnderJoin[0].equals(oldNonOptSubPlan) ) {
			return inputPlan;
		}

		// Create the new rewritten plan to be returned.
		final LogicalPlan newSubPlanUnderFilter = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayLeftJoin.getInstance(),
		                                                                           newSubPlansUnderJoin );
		if ( newFilterOp == null )
			return newSubPlanUnderFilter;
		else
			return new LogicalPlanWithUnaryRootImpl(newFilterOp, newSubPlanUnderFilter);
	}



	protected LogicalPlan pushToNonOptSubPlan( final ExprList filterExprsWithoutAND,
	                                           final LogicalPlan nonoptSubPlan,
	                                           final ExprList toBeKept ) {
		// Determine the set of certain variables in the non-optional subplan,
		// which will be needed for checking which of the filter conditions may
		// be pushed to that subplan.
		final ExpectedVariables expVarsInSubPlan = nonoptSubPlan.getExpectedVariables();
		final Set<Var> certainVarsInSubPlan = expVarsInSubPlan.getCertainVariables();

		// Determine which of the filter conditions may be pushed to
		// the non-optional subplan and which ones need to be kept to
		// be evaluated after the join.
		ExprList toBePushed = null;

		for ( final Expr e : filterExprsWithoutAND ) {
			// If all of the variables mentioned in the current filter
			// condition are certain variables of the non-optional subplan,
			// then the condition can be pushed to the subplan.
			final Set<Var> varsInExpr = ExprVars.getVarsMentioned(e);
			if ( certainVarsInSubPlan.containsAll(varsInExpr) ) {
				if ( toBePushed == null )
					toBePushed = new ExprList(e);
				else
					toBePushed.add(e);
			}
			else {
				toBeKept.add(e);
			}
		}

		// Now create the new non-optional subplan.
		if ( toBePushed == null ) {
			// If there are no filter conditions to be pushed to
			// the subplan, then only apply the heuristic to it.
			return apply(nonoptSubPlan);
		}
		else {
			// There are filter conditions to be pushed to the subplan.
			final LogicalOpFilter filterForSubPlan = new LogicalOpFilter(toBePushed);
			final LogicalPlan newSubPlanWithFilterAsRoot = new LogicalPlanWithUnaryRootImpl(filterForSubPlan, nonoptSubPlan);
			final LogicalPlan newSubPlanWithFilterPushed = apply(newSubPlanWithFilterAsRoot);
			return newSubPlanWithFilterPushed;
		}
	}

	protected LogicalPlan createPlanAfterPushingInSubPlan( final LogicalOpFilter parentFilterOp,
	                                                       final UnaryLogicalOp childOp,
	                                                       final LogicalPlan subPlanUnderChildOp,
	                                                       final LogicalPlan inputPlan ) {
		// Apply the heuristic recursively within the given subplan. 
		final LogicalPlan newPlanUnderChildRoot = apply(subPlanUnderChildOp);

		// If the heuristic cannot be applied within the subplan (more
		// precisely, if there are no filters in the subplan that can
		// be pushed), then we return the given input plan (instead of
		// recreating another, identical version of that plan).
		if ( newPlanUnderChildRoot.equals(subPlanUnderChildOp) ) {
			return inputPlan;
		}

		// After pushing filters in the subplan, create a new plan.
		final LogicalPlan newPlanUnderFilter = new LogicalPlanWithUnaryRootImpl(childOp, newPlanUnderChildRoot);
		final LogicalPlan newPlan = new LogicalPlanWithUnaryRootImpl(parentFilterOp, newPlanUnderFilter);
		return newPlan;
	}

	protected ExprList splitConjunctions( final ExprList l ) {
		final ExprList result = new ExprList();
		for ( final Expr e : l ) {
			if ( e instanceof E_LogicalAnd ) {
				split( (E_LogicalAnd) e, result );
			}
			else {
				result.add(e);
			}
		}

		return ( l.size() == result.size() ) ? l : result;
	}

	protected void split( final E_LogicalAnd e, final ExprList l ) {
		final Expr e1 = e.getArg1();
		final Expr e2 = e.getArg2();

		if ( e1 instanceof E_LogicalAnd ) {
			split( (E_LogicalAnd) e1, l );
		}
		else {
			l.add(e1);
		}

		if ( e2 instanceof E_LogicalAnd ) {
			split( (E_LogicalAnd) e2, l );
		}
		else {
			l.add(e2);
		}
	}

}
