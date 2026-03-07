package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.randomized;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.PlanRewritingUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleInstances;

public abstract class RandomizedQueryOptimizerBase implements PhysicalOptimizer
{
	protected final Random rng = new Random();

	protected final LogicalToPhysicalPlanConverter l2pConverter;
	protected final CostModel costModel;
	protected final PlanRewritingUtils rules;

	protected RandomizedQueryOptimizerBase( final LogicalToPhysicalPlanConverter l2pConverter,
	                                        final CostModel costModel,
	                                        final RuleInstances rewritingRules ) {
		assert l2pConverter != null;
		assert costModel != null;

		this.l2pConverter = l2pConverter;
		this.costModel = costModel;

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
