package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.*;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula.FmAwareWeightedJoinAndUnboundVariableCount;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula.Formula;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula.JoinAwareWeightedUnboundVariableCount;

public class LogicalOptimizerImpl implements LogicalOptimizer
{
	protected final List<HeuristicForLogicalOptimization> heuristics = new ArrayList<>();

	public LogicalOptimizerImpl() {
		final HeuristicForLogicalOptimization mergeRequests = new MergeRequests();

		heuristics.add( new UnionPullUp() );
		heuristics.add( mergeRequests );
		heuristics.add( new FilterPushDown() );
		heuristics.add( mergeRequests );

//		final Formula formula = new JoinAwareWeightedUnboundVariableCount();
//		heuristics.add( new GreedyBasedReordering( formula ) );

//		heuristics.add( new PushJoinUnderUnionWithRequests() );

/*
  Uncomment the following line to apply vocabulary mappings of federation members during query planning.
  Note that applying vocabulary mappings is only supported by the naive algorithm currently
  since the cost model needs to be extended to consider operators PhysicalOpLocalToGlobal and PhysicalOpGlobalToLocal
 */
//		heuristics.add( new ApplyVocabularyMappings());
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
