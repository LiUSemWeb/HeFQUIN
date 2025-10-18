package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

/**
 * In cases in which there are unions with requests under joins (which may
 * happen only if we do not use {@link UnionPullUp}, this heuristics turns
 * the requests into gpAdd operators with the previous join arguments as
 * subplans. The rationale of this heuristics is that it allows us to use
 * bind joins and index nested loops joins in cases in which otherwise only
 * local join algorithms (e.g., symmetric hash join) could be used. In this
 * sense, the effect of this heuristics is similar to that of {@link UnionPullUp},
 * but without pulling out unions completely and, thus, without producing
 * a great number of join subplans.
 *
 * For instance, consider the following plan.
 *
 * <br/>
 * <code>
 * mj(
 *     req_fm1^(s,p,?x),
 *     mu(
 *         req_fm2^(?x,p2,?y),
 *         req_fm3^(?x,p2,?y)
 *     ),
 *     mu(
 *         req_fm2^(?y,p3,?z),
 *         req_fm3^(?y,p3,?z)
 *     )
 * )
 * </code>
 * <br/>
 * 
 * In this case, the joins can be implemented only by using a local join
 * algorithm (e.g., symmetric hash join), which also means that the requests
 * inside the unions are executed by physical request operators and, thus,
 * may end up retrieving a lot of unnecessary triples from fm2 and from fm3
 * (i.e., triples that do not participate in the joins). In contrast, when
 * applying this heuristic to that plan, the resulting plan is:
 *
 * <br/>
 * <code>
 * mu(
 *     gpAdd_fm2^(?y,p3,?z) (
 *         mu(
 *             gpAdd__fm2^(?x,p2,?y) (
 *                 req_fm1^(s,p,?x)
 *             ),
 *             gpAdd__fm3^(?x,p2,?y) (
 *                 req_fm1^(s,p,?x)
 *             )
 *         )
 *     ),
 *     gpAdd_fm3^(?y,p3,?z) (
 *         mu(
 *             ...
 *         )
 *     )
 * ),
 * </code>
 * <br/>
 *
 * where the subplan under the second gpAdd operator is the same as the
 * subplan under the first gpAdd operator. Now, the gpAdd operators can
 * be implemented using bind joins or index nested loops joins, which may
 * reduce the number of unnecessary triples retrieved from fm2 and fm3.
 *
 * The heuristic assumes that we have already applied a join ordering
 * heuristics beforehand; i.e., the subplans under the multiway joins
 * already have a reasonable order before this heuristics turns such
 * a multiway join into stages of gpAdd operators (with the union
 * operators in between the stages). Based on this assumption, the
 * heuristics always processes the subplans of multiway joins from
 * left to right; i.e., the left-most union subplan becomes the first
 * stage and the right-most union subplan becomes the last stage at
 * the top of the resulting (sub)plan.
 *
 * If there are multiple subplans under a join that are not union subplans,
 * then these are kept together under a join. For instance, if the previous
 * example plan would contain a second request before the multiway unions,
 * then that request would be kept together with the first request as a join
 * under the tpAdd operators created for the first multiway union.
 *
 * In addition to requests under unions (that are under joins), this heuristic
 * even considers combinations of a filter operator with a request operator,
 * in which case the request would also be turned into an gpAdd operator, but
 * with the filter on top of it. Moreover, any other subplan under such unions
 * (that are under joins) is joined with the previous join arguments. To
 * illustrate these cases, consider the following example plan.
 *
 * <br/>
 * <code>
 * mj(
 *     req_fm1^(s,p,?x),
 *     mu(
 *         filter^condition(
 *             req_fm2^(?x,p2,?y)
 *         ),
 *         leftjoin( ... )
 *     )
 * )
 * </code>
 * <br/>
 *
 * The resulting plan would then be as follows.
 *
 * <br/>
 * <code>
 * mu(
 *     filter^condition(
 *         gpAdd_fm2^(?x,p2,?y) (
 *             req_fm1^(s,p,?x)
 *         )
 *     ),
 *     mj(
 *         req_fm1^(s,p,?x),
 *         leftjoin( ... )
 *     )
 * )
 * </code>
 * <br/>
 *
 */
public class PushJoinUnderUnionWithRequests implements HeuristicForLogicalOptimization
{
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		// First, apply the heuristic recursively to all subplans.
		final LogicalPlan[] rewrittenSubPlans = new LogicalPlan[numberOfSubPlans];
		boolean noChanges = true;
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan subPlan = inputPlan.getSubPlan(i);
			final LogicalPlan rewrittenSubPlan = apply(subPlan);

			if ( rewrittenSubPlan.equals(subPlan) ) {
				rewrittenSubPlans[i] = subPlan;
			}
			else {
				rewrittenSubPlans[i] = rewrittenSubPlan;
				noChanges = false;
			}
		}

		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if (    !(rootOp instanceof LogicalOpJoin)
		     && !(rootOp instanceof LogicalOpMultiwayJoin) )
		{
			if ( noChanges )
				return inputPlan;
			else
				return LogicalPlanUtils.createPlanWithSubPlans(rootOp, rewrittenSubPlans);
		}

		if ( numberOfSubPlans == 1 ) return rewrittenSubPlans[0];

		List<LogicalPlan> subPlansAsNextInput = new ArrayList<>();
		subPlansAsNextInput.add( rewrittenSubPlans[0] );
		int i = 1;
		while ( i < numberOfSubPlans ) {
			final LogicalPlan currSubPlan = rewrittenSubPlans[i];
			final LogicalOperator subRootOp = currSubPlan.getRootOperator();

			if (    subRootOp instanceof LogicalOpUnion
			     || subRootOp instanceof LogicalOpMultiwayUnion )
			{
				noChanges = false;

				if ( currSubPlan.numberOfSubPlans() == 1 ) {
					subPlansAsNextInput.add( currSubPlan.getSubPlan(0) );
				}
				else {
					final LogicalPlan currSubPlanNew = rewrite(currSubPlan, subPlansAsNextInput);

					subPlansAsNextInput = new ArrayList<>();
					subPlansAsNextInput.add(currSubPlanNew);
				}
			}
			else {
				subPlansAsNextInput.add(currSubPlan);
			}

			i++;
		}

		if ( noChanges ) return inputPlan;

		if ( subPlansAsNextInput.size() == 1 )
			return subPlansAsNextInput.get(0);
		else
			return LogicalPlanUtils.createPlanWithMultiwayJoin(subPlansAsNextInput);
	}

	protected LogicalPlan rewrite( final LogicalPlan unionPlan, final List<LogicalPlan> subPlansAsInput ) {
		final LogicalPlan inputPlan;
		if ( subPlansAsInput.size() == 1 )
			inputPlan = subPlansAsInput.get(0);
		else
			inputPlan = LogicalPlanUtils.createPlanWithMultiwayJoin(subPlansAsInput);

		final int numberOfSubPlansUnderUnion = unionPlan.numberOfSubPlans();
		final LogicalPlan[] newUnionSubPlans = new LogicalPlan[numberOfSubPlansUnderUnion];
		for ( int i = 0; i < numberOfSubPlansUnderUnion; i++ ) {
			final LogicalPlan oldSubPlan = unionPlan.getSubPlan(i);
			final LogicalPlan newSubPlan;

			final LogicalOperator oldSubPlanRootOp = oldSubPlan.getRootOperator();
			if ( oldSubPlanRootOp instanceof LogicalOpRequest reqOp ) {
				final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromLogicalReqOp(reqOp);
				final LogicalPlan addOpPlan = new LogicalPlanWithUnaryRootImpl(addOp, inputPlan);
				newSubPlan = addOpPlan;
			}
			else if (    oldSubPlanRootOp instanceof LogicalOpFilter filterOp
			          && oldSubPlan.getSubPlan(0).getRootOperator() instanceof LogicalOpRequest ) {
				final LogicalOpRequest<?,?> reqOp = (LogicalOpRequest<?,?>) oldSubPlan.getSubPlan(0).getRootOperator();
				final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromLogicalReqOp(reqOp);
				final LogicalPlan addOpPlan = new LogicalPlanWithUnaryRootImpl(addOp, inputPlan);
				final LogicalPlan filterOpPlan = new LogicalPlanWithUnaryRootImpl(filterOp, addOpPlan);
				newSubPlan = filterOpPlan;
			}
			else if ( oldSubPlanRootOp instanceof LogicalOpLocalToGlobal l2gLop
					&& oldSubPlan.getSubPlan(0).getRootOperator() instanceof LogicalOpRequest ) {
				final LogicalOpGlobalToLocal g2l = new LogicalOpGlobalToLocal( l2gLop.getVocabularyMapping() );
				final LogicalPlan newInputPlan = new LogicalPlanWithUnaryRootImpl( g2l, inputPlan );

				final LogicalOpRequest<?,?> reqOp = (LogicalOpRequest<?, ?>) oldSubPlan.getSubPlan(0).getRootOperator();
				final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromLogicalReqOp(reqOp);
				final LogicalPlan addOpPlan = new LogicalPlanWithUnaryRootImpl(addOp, newInputPlan);

				newSubPlan = new LogicalPlanWithUnaryRootImpl( l2gLop, addOpPlan );
			}
			else if ( oldSubPlanRootOp instanceof LogicalOpFilter filterOp
					&& oldSubPlan.getSubPlan(0).getRootOperator() instanceof LogicalOpLocalToGlobal
					&& oldSubPlan.getSubPlan(0).getSubPlan(0).getRootOperator() instanceof LogicalOpRequest ) {
				final LogicalOpLocalToGlobal l2gLop = (LogicalOpLocalToGlobal) oldSubPlan.getSubPlan(0).getRootOperator();
				final LogicalOpGlobalToLocal g2l = new LogicalOpGlobalToLocal( l2gLop.getVocabularyMapping() );
				final LogicalPlan newInputPlan = new LogicalPlanWithUnaryRootImpl( g2l, inputPlan );

				final LogicalOpRequest<?,?> reqOp = (LogicalOpRequest<?, ?>) oldSubPlan.getSubPlan(0).getSubPlan(0).getRootOperator();
				final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromLogicalReqOp(reqOp);
				final LogicalPlan addOpPlan = new LogicalPlanWithUnaryRootImpl(addOp, newInputPlan);

				final LogicalPlan l2gOpPlan = new LogicalPlanWithUnaryRootImpl( l2gLop, addOpPlan );
				newSubPlan = new LogicalPlanWithUnaryRootImpl( filterOp, l2gOpPlan );
			}
			else {
				newSubPlan = LogicalPlanUtils.createPlanWithMultiwayJoin(inputPlan, oldSubPlan);
			}

			newUnionSubPlans[i] = newSubPlan; 
		}

		return LogicalPlanUtils.createPlanWithMultiwayUnion(newUnionSubPlans);
	}

}
