package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.PlanRewritingUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.utils.Pair;

public abstract class RandomizedQueryOptimizerBase implements QueryOptimizer
{
	protected final Random rng = new Random();

	protected final PlanRewritingUtils rules;
	protected final QueryOptimizationContext context;

	protected RandomizedQueryOptimizerBase( final QueryOptimizationContext context,
	                                        final RuleInstances rewritingRules ) {
		assert context != null;
		this.context = context;

		rules = new PlanRewritingUtils(rewritingRules);
	}

	@Override
	public Pair<PhysicalPlan, QueryOptimizationStats> optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException {
		final PhysicalPlan bestPlan = null; // TODO: fix after merging into main!!

        final QueryOptimizationStats myStats = new QueryOptimizationStatsImpl();

		return new Pair<>(bestPlan, myStats);		
	}

	protected List<PhysicalPlan> getNeighbours( final PhysicalPlan initialPlan ) {
		final Set<RuleApplication> ruleApplications = rules.getRuleApplications(initialPlan);

		final List<PhysicalPlan> resultList = new ArrayList<PhysicalPlan>();
		for ( final RuleApplication ra : ruleApplications ) {
			resultList.add( ra.getResultingPlan() );
		}

		return resultList;
	}
	
	protected <T> T getRandomElement( final List<T> L ) {
		if(L.size() == 1) {
			return L.get(0);
		} else {
			return L.get( rng.nextInt(L.size()) ); // Get a random object.
		}
	}
	
}
