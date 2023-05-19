package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.*;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula.FmAwareWeightedJoinAndUnboundVariableCount;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula.FormulaForComputingSelectivity;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula.JoinAwareWeightedUnboundVariableCount;

public class LogicalOptimizerImpl implements LogicalOptimizer
{
	protected final List<HeuristicForLogicalOptimization> heuristics = new ArrayList<>();
	protected final QueryProcContext ctxt;

	public LogicalOptimizerImpl( final QueryProcContext ctxt ) {
		assert ctxt != null;
		this.ctxt = ctxt;

		final HeuristicForLogicalOptimization mergeRequests = new MergeRequests();

		//// It has turned out that UnionPullUp typically does more
		//// harm than good. For more details, see the end of the
		//// JavaDoc comment for UnionPullUp.
		//heuristics.add( new UnionPullUp() );

		heuristics.add( mergeRequests );
		heuristics.add( new FilterPushDown() );
		heuristics.add( mergeRequests );

		// final FormulaForComputingSelectivity formula = new JoinAwareWeightedUnboundVariableCount();
		// heuristics.add( new GreedyBasedReordering(formula) );

		// heuristics.add( new PushJoinUnderUnionWithReuests() );

		//// Uncomment the following line to apply vocabulary mappings of
		//// federation members during query planning. Note that applying
		//// vocabulary mappings is not supported by the evolutionary algorithm
		//// currently since the rewriting rules need to be extended to consider
		//// operators PhysicalOpLocalToGlobal and PhysicalOpGlobalToLocal.
		// heuristics.add( new ApplyVocabularyMappings(false) );
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
