package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.randomized;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.PlanRewritingUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleInstances;

public abstract class RandomizedQueryOptimizerBase implements PhysicalQueryOptimizer
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

	protected List<PhysicalPlan> getNeighbours( final PhysicalPlan initialPlan ) {
		final Set<RuleApplication> ruleApplications = rules.getRuleApplications(initialPlan);

		final List<PhysicalPlan> resultList = new ArrayList<PhysicalPlan>();
		for ( final RuleApplication ra : ruleApplications ) {
			resultList.add( ra.getResultingPlan() );
		}

		return resultList;
	}

	protected <T> T getRandomElement( final List<T> L ) {
		if ( L.size() == 1 ) {
			return L.get(0);
		}
		else {
			return L.get( rng.nextInt(L.size()) ); // Get a random object.
		}
	}
	
}
