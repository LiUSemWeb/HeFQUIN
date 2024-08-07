package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.*;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula.*;

public class HeuristicsBasedLogicalOptimizerImpl implements LogicalOptimizer
{
	protected final List<HeuristicForLogicalOptimization> heuristics;
	protected final QueryProcContext ctxt;

	public HeuristicsBasedLogicalOptimizerImpl( final QueryProcContext ctxt, final List<HeuristicForLogicalOptimization> heuristics ) {
		assert ctxt != null;
		this.ctxt = ctxt;
		this.heuristics = heuristics;
	}

	public static List<HeuristicForLogicalOptimization> getDefaultHeuristics( final QueryProcContext ctxt ) {
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
		heuristics.add( new CardinalityBasedJoinOrderingWithRequests(ctxt) );

		heuristics.add( new RemoveUnnecessaryL2gAndG2l() );

		heuristics.add( new PullUpLtgOverUnion() );
		heuristics.add( new PullUpLtgOverJoin() );

		heuristics.add( new PushJoinUnderUnionWithRequests() );
		heuristics.add( new PullUpLtgOverUnion() );
		heuristics.add( new RemovePairsOfG2lAndL2g() );

		return heuristics;
	}

	@Override
	public LogicalPlan optimize( final LogicalPlan inputPlan, final boolean keepNaryOperators ) throws LogicalOptimizationException {
		LogicalPlan resultPlan = inputPlan;
		for ( final HeuristicForLogicalOptimization h : heuristics ) {
			resultPlan = h.apply(resultPlan);
		}

		return resultPlan;
	}

}
