package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithoutResult;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext2;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.*;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula.*;

public class HeuristicsBasedLogicalOptimizerImpl implements LogicalOptimizer
{
	private static final Logger log = LoggerFactory.getLogger( HeuristicsBasedLogicalOptimizerImpl.class );

	protected final List<HeuristicForLogicalOptimization> heuristics;

	public HeuristicsBasedLogicalOptimizerImpl( final List<HeuristicForLogicalOptimization> heuristics ) {
		assert heuristics != null;
		this.heuristics = heuristics;
	}

	public static List<HeuristicForLogicalOptimization> getDefaultHeuristics() {
		final List<HeuristicForLogicalOptimization> heuristics = new ArrayList<>();

		final HeuristicForLogicalOptimization mergeRequests = new MergeRequests();

		//// It has turned out that UnionPullUp typically does more
		//// harm than good. For more details, see the end of the
		//// JavaDoc comment for UnionPullUp.
		//heuristics.add( new UnionPullUp() );

		heuristics.add( mergeRequests );
		heuristics.add( new FilterPushDown() );
		heuristics.add( mergeRequests );

		final FormulaForComputingSelectivity formula = new JoinAwareWeightedUnboundVariableCount();
		heuristics.add( new GreedyBasedReordering(formula) );

		//// Uncomment the following line to apply vocabulary mappings of
		//// federation members during query planning. Note that applying
		//// vocabulary mappings is not supported by the evolutionary algorithm
		//// currently since the rewriting rules need to be extended to consider
		//// operators PhysicalOpLocalToGlobal and PhysicalOpGlobalToLocal.
		heuristics.add( new ApplyVocabularyMappings() );
		heuristics.add( new CardinalityBasedJoinOrderingWithRequests() );

		heuristics.add( new RemoveUnnecessaryL2gAndG2l() );

		heuristics.add( new PullUpLtgOverUnion() );
		heuristics.add( new PullUpLtgOverJoin() );

		heuristics.add( new PushJoinUnderUnionWithRequests() );
		heuristics.add( new PullUpLtgOverUnion() );
		heuristics.add( new RemovePairsOfG2lAndL2g() );

		return heuristics;
	}

	@Override
	public LogicalPlan optimize( final LogicalPlan inputPlan,
	                             final boolean keepNaryOperators,
	                             final QueryProcContext2 ctxt ) throws LogicalOptimizationException {
		log.debug( "Starting logical optimization with {} heuristics", heuristics.size() );
		LogicalPlan resultPlan = inputPlan;
		for ( final HeuristicForLogicalOptimization h : heuristics ) {
			log.debug( "Applying heuristic {} to plan", h.getClass().getSimpleName() );
			resultPlan = h.apply(resultPlan, ctxt);
			log.debug( "Finished applying heuristic {} to plan", h.getClass().getSimpleName() );

			// If the plan has been rewritten into the plan that produces
			// the empty result, then this plan can be returned immediately.
			if ( resultPlan instanceof LogicalPlanWithoutResult ) {
				log.debug( "Optimization terminated early: heuristic {} produced empty-result plan", h.getClass().getSimpleName() );
				return resultPlan;
			}
		}

		log.debug( "Logical optimization finished" );

		return resultPlan;
	}

}
