package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.*;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

/**
 * Pulls up all (multiway) union operators in the given plan as high up as
 * possible. The rationale of this heuristics is that it opens more options
 * when doing the join optimization during the physical query optimization.
 *
 * For instance, consider the following plan:
 * <br/>
 * <code>
 * mj(
 *     req_fm1^(s,p,?x),
 *     mu(
 *         req_fm2^(?x,?y,?z),
 *         req_fm3^(?x,?y,?z)
 *     )
 * )
 * </code>
 * <br/>
 * 
 * In this case, the join can be implemented only by using a local join
 * algorithm (e.g., hash join), which also means that the two requests
 * within the union are executed by physical request operators and, thus,
 * end up retrieving all triples from fm2 and from fm3.
 * In contrast, after pulling up the union operator, the resulting plan
 * is:
 * 
 * <br/>
 * <code>
 * mu(
 *     mj(
 *         req_fm1^(s,p,?x),
 *         req_fm2^(?x,?y,?z)
 *     ),
 *     mj(
 *         req_fm1^(s,p,?x),
 *         req_fm3^(?x,?y,?z)
 *     )
 * )
 * </code>
 * <br/>
 * 
 * Now, the two joins can be turned into bind joins, and it is even possible
 * to pick two different join strategies for them.
 * 
 * Attention: It has turned out that UnionPullUp typically does more harm
 * than good, because it has the tendency to turn a join-over-union source
 * assignment into a (multiway) union with many join subplans (in particular
 * for cases in which there are several unions in the join-over-union source
 * assignment, because in these cases every combination of requests under
 * the different unions ends up as a separate join). If, thereafter, these
 * many join subplans are predominantly implemented as symmetric hash joins,
 * then the execution plan will do many more requests than what would be
 * necessary for the joins of the original join-over-union source assignment.
 * An alternative heuristic that does not have this problem is
 * {@link PushJoinUnderUnionWithRequests}.
 */
public class UnionPullUp implements HeuristicForLogicalOptimization
{

	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		// First, apply the heuristic recursively to all subplans. When doing
		// so, already separate the (rewritten) subplans that have a union as
		// their root operator from the subplans that do not have a union as
		// root. Additionally, keep track of whether there was at least one
		// subplan that was actually rewritten.
		boolean subPlansDiffer = false; // will be set to true if at least one of the subplans is rewritten
		final List<LogicalPlan> rewrittenSubPlansWithUnionRoot = new ArrayList<>();
		final List<LogicalPlan> rewrittenSubPlansWithNonUnionRoot = new ArrayList<>();
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan subPlan = inputPlan.getSubPlan(i);
			final LogicalPlan rewrittenSubPlan = apply(subPlan);

			if ( hasUnionRoot(rewrittenSubPlan) )
				rewrittenSubPlansWithUnionRoot.add(rewrittenSubPlan);
			else
				rewrittenSubPlansWithNonUnionRoot.add(rewrittenSubPlan);

			if ( ! subPlansDiffer ) { // check for equivalence only if necessary 
				if ( ! subPlan.equals(rewrittenSubPlan) )
					subPlansDiffer = true;
			}
		}

		// Next, apply the heuristic to the root of the plan if possible.
		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if (    rootOp instanceof LogicalOpGPAdd
		     || rootOp instanceof LogicalOpGPOptAdd
		     || rootOp instanceof LogicalOpFilter
		     || rootOp instanceof LogicalOpLocalToGlobal
		     || rootOp instanceof LogicalOpGlobalToLocal )
		{
			// The listed operators are unary operators; i.e., have exactly one
			// subplan as child. If that subplan has a union operator as root,
			// then pull up the union (i.e., push the unary operator to be the
			// root of each of the children of the union).
			if ( ! rewrittenSubPlansWithUnionRoot.isEmpty() )
				return rewritePlanWithUnaryRootAndUnionChild( (UnaryLogicalOp) rootOp,
				                                              rewrittenSubPlansWithUnionRoot.get(0) );
		}
		else if ( rootOp instanceof LogicalOpUnion || rootOp instanceof LogicalOpMultiwayUnion )
		{
			if ( ! rewrittenSubPlansWithUnionRoot.isEmpty() )
				return rewritePlanWithUnionRootAndUnionChild( rewrittenSubPlansWithUnionRoot,
				                                              rewrittenSubPlansWithNonUnionRoot );
		}
		else if ( rootOp instanceof LogicalOpJoin || rootOp instanceof LogicalOpMultiwayJoin )
		{
			if ( ! rewrittenSubPlansWithUnionRoot.isEmpty() )
				return rewritePlanWithJoinOverUnion( rewrittenSubPlansWithUnionRoot,
				                                     rewrittenSubPlansWithNonUnionRoot );
		}
		else if ( rootOp instanceof LogicalOpRightJoin )
		{
			// nothing to do here (if we have an outer join as root operator,
			// we do not attempt to pull a potential union out of it)

			// TODO: think about this case again
		}
		else if ( rootOp instanceof LogicalOpMultiwayLeftJoin )
		{
			// nothing to do here (if we have an outer join as root operator,
			// we do not attempt to pull a potential union out of it)

			// TODO: think about this case again
		}
		else {
			throw new IllegalArgumentException( rootOp.getClass().getName() );
		}

		// Finally, if the heuristic was not applied to the root of
		// the plan, return the plan without changing its root, but
		// make sure to use the rewritten subplans if necessary.
		if ( subPlansDiffer ) {
			final List<LogicalPlan> rewrittenSubPlans = new ArrayList<>(numberOfSubPlans);
			rewrittenSubPlans.addAll(rewrittenSubPlansWithUnionRoot);
			rewrittenSubPlans.addAll(rewrittenSubPlansWithNonUnionRoot);

			return LogicalPlanUtils.createPlanWithSubPlans(rootOp, rewrittenSubPlans);
		}
		else
			return inputPlan;
	}


	public boolean hasUnionRoot( final LogicalPlan plan ) {
		return plan.getRootOperator() instanceof LogicalOpUnion ||
		       plan.getRootOperator() instanceof LogicalOpMultiwayUnion;
	}


	/**
	 * Based on the assumption that the given subplan has a union operator
	 * as its root operator, this function pulls up that union by pushing
	 * the given unary operator to be the root of each of the children of
	 * the union.
	 */
	public LogicalPlan rewritePlanWithUnaryRootAndUnionChild( final UnaryLogicalOp rootOp,
	                                                          final LogicalPlan subPlan ) {
		final int numberOfSubPlansUnderUnion = subPlan.numberOfSubPlans();
		final List<LogicalPlan> newSubPlans = new ArrayList<>(numberOfSubPlansUnderUnion);
		for ( int i = 0; i < numberOfSubPlansUnderUnion; i++ ) {
			final LogicalPlan subPlanUnderUnion = subPlan.getSubPlan(i);
			final LogicalPlan newSubPlan = new LogicalPlanWithUnaryRootImpl(rootOp, subPlanUnderUnion);
			newSubPlans.add(newSubPlan);
		}

		return new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), newSubPlans );
	}

	/**
	 * Based on the assumptions that i) the plans in the given lists have all
	 * been subplans of a union and ii) the given list of union-rooted plans
	 * is nonempty, merges all these union-rooted plans (plansWithUnionRoot)
	 * into a single union-root plan that has all the subplans of the given
	 * union-rooted plans as its subplans, together with all the plans given
	 * in the other list (plansWithNonUnionRoot).
	 */
	public LogicalPlan rewritePlanWithUnionRootAndUnionChild( final List<LogicalPlan> plansWithUnionRoot,
	                                                          final List<LogicalPlan> plansWithNonUnionRoot ) {
		final List<LogicalPlan> newSubPlans = new ArrayList<>();

		for ( final LogicalPlan planWithUnionRoot : plansWithUnionRoot ) {
			final int numberOfSubPlansUnderUnion = planWithUnionRoot.numberOfSubPlans();
			for ( int i = 0; i < numberOfSubPlansUnderUnion; i++ ) {
				newSubPlans.add( planWithUnionRoot.getSubPlan(i) );
			}
		}

		newSubPlans.addAll(plansWithNonUnionRoot);

		return new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), newSubPlans );
		
	}

	/**
	 * Based on the assumptions that i) the plans in the given lists have all
	 * been subplans of a join and ii) the given list of union-rooted plans is
	 * nonempty, pulls the unions out of these union-rooted plans by joining
	 * all their subplans with one another, and also with all the plans given
	 * in the other list (plansWithNonUnionRoot).
	 */
	public LogicalPlan rewritePlanWithJoinOverUnion( final List<LogicalPlan> plansWithUnionRoot,
	                                                 final List<LogicalPlan> plansWithNonUnionRoot ) {
		final Iterator<LogicalPlan> it = plansWithUnionRoot.iterator();

		final LogicalPlan firstPlanWithUnionRoot = it.next();
		List<List<LogicalPlan>> unionsOverJoins = new ArrayList<>( firstPlanWithUnionRoot.numberOfSubPlans() );
		for ( int i = 0; i < firstPlanWithUnionRoot.numberOfSubPlans(); i++ ) {
			final List<LogicalPlan> join = new ArrayList<>();
			join.add( firstPlanWithUnionRoot.getSubPlan(i) );
			unionsOverJoins.add(join);
		}

		while ( it.hasNext() ) {
			final LogicalPlan nextPlanWithUnionRoot = it.next();
			final List<List<LogicalPlan>> unionsOverJoins2 = new ArrayList<>( unionsOverJoins.size() * nextPlanWithUnionRoot.numberOfSubPlans() );
			for ( int i = 0; i < nextPlanWithUnionRoot.numberOfSubPlans(); i++ ) {
				for ( final List<LogicalPlan> join : unionsOverJoins ) {
					final List<LogicalPlan> join2 = new ArrayList<>();
					join2.addAll(join);
					join2.add( nextPlanWithUnionRoot.getSubPlan(i) );
					unionsOverJoins2.add(join2);
				}
			}

			unionsOverJoins = unionsOverJoins2;
		}

		final List<LogicalPlan> newSubPlans = new ArrayList<>( unionsOverJoins.size() );
		for ( final List<LogicalPlan> join : unionsOverJoins ) {
			join.addAll(plansWithNonUnionRoot);

			final LogicalPlan newSubPlan = new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayJoin.getInstance(), join );
			newSubPlans.add(newSubPlan);
		}

		return new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(), newSubPlans ); 
	}

}
