package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.RuleApplicationsOfPlans;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;

public abstract class RandomizedQueryOptimizerBase implements QueryOptimizer
{
	protected final Random rng                    = new Random();
	protected final RuleApplicationsOfPlans rules = new RuleApplicationsOfPlans( new RuleInstances() );

	protected final QueryOptimizationContext context;

	protected RandomizedQueryOptimizerBase( final QueryOptimizationContext context ) {
		assert context != null;
		this.context = context;
	}

	@Override
	public PhysicalPlan optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException {
		return optimize( context.getLogicalToPhysicalPlanConverter().convert(initialPlan,false) );
	}

	abstract public PhysicalPlan optimize( PhysicalPlan initialPlan ) throws QueryOptimizationException;


	protected List<PhysicalPlan> getNeighbours( final PhysicalPlan initialPlan ) {
		final Set<RuleApplication> ruleApplications = rules.getRuleApplications(initialPlan);

		final List<PhysicalPlan> resultList = new ArrayList<PhysicalPlan>();
		for ( final RuleApplication ra : ruleApplications ) {
			resultList.add( ra.getResultingPlan() );
		}

		return resultList;
	}

}
