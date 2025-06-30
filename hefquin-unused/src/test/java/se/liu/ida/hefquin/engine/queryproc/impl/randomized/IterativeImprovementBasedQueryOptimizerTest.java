package se.liu.ida.hefquin.engine.queryproc.impl.randomized;

import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverterImpl;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.randomized.IterativeImprovementBasedQueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.randomized.StoppingConditionByNumberOfGenerations;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.randomized.StoppingConditionForIterativeImprovement;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RewritingRule;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleInstances;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class IterativeImprovementBasedQueryOptimizerTest extends EngineTestBase
{
	@Test
	public void iiOptimizerTest1() throws PhysicalOptimizationException {
		// set up everything for the test
		final PhysicalPlan initialPlan = new DummyPlanForTest(10);
		final PhysicalPlan plan1 = new DummyPlanForTest(8);
		final PhysicalPlan plan2 = new DummyPlanForTest(9);
		final PhysicalPlan plan3 = new DummyPlanForTest(5);

		final RuleApplication ra1 = new DummyRuleApplicationForTest(plan1);
		final RuleApplication ra2 = new DummyRuleApplicationForTest(plan2);
		final RuleApplication ra3 = new DummyRuleApplicationForTest(plan3);

		final RewritingRule dummyRuleForTest = new RewritingRule() {
			@Override public double getPriority() { return 0; }

			@Override
			public Set<RuleApplication> determineAllPossibleApplications( final PhysicalPlan plan ) {
				final Set<RuleApplication> s = new HashSet<>();
				if ( plan.equals(initialPlan) ) { s.add(ra1); s.add(ra2); }
				if ( plan.equals(plan1) ) { s.add(ra3); }
				if ( plan.equals(plan2) ) { s.add(ra1); }
				if ( plan.equals(plan3) ) { s.add(ra1); s.add(ra3); }
				return s;
			}
		};

		final RuleInstances rewritingRules = new RuleInstances();
		rewritingRules.ruleInstances.clear();
		rewritingRules.ruleInstances.add(dummyRuleForTest);

		final StoppingConditionForIterativeImprovement cond = new StoppingConditionByNumberOfGenerations(5);
		final IterativeImprovementBasedQueryOptimizer o = new IterativeImprovementBasedQueryOptimizer( cond, l2pConverter, costModel, rewritingRules );

		// run the test
		final PhysicalPlan resultingPlan = o.optimize( initialPlan ).object1;

		// verify that the outcome is correct
		assertEquals( plan3, resultingPlan );
	}


	// ----------- helper code for the tests ------------

	final LogicalToPhysicalPlanConverter l2pConverter = new LogicalToPhysicalPlanConverterImpl(true, true);

	final CostModel costModel = new CostModel() {
		@Override
		public CompletableFuture<Double> initiateCostEstimation( final PhysicalPlan p ) {
			final DummyPlanForTest pp = (DummyPlanForTest) p;
			return CompletableFuture.completedFuture( pp.cost );
		}
	};

	protected static class DummyPhysicalOperatorForTest implements PhysicalOperatorForLogicalOperator {
		@Override
		public ExecutableOperator createExecOp(boolean collectExceptions, ExpectedVariables... inputVars) {
			return null;
		}

		@Override
		public ExpectedVariables getExpectedVariables(ExpectedVariables... inputVars) {
			return null;
		}

		@Override
		public void visit(PhysicalPlanVisitor visitor) {
			
		}

		@Override
		public LogicalOperator getLogicalOperator() {
			return null;
		}

		@Override
		public int getID() {
			return 0;
		}
	}
	
	protected static class DummyPlanForTest implements PhysicalPlan {
		public final double cost;
		public DummyPlanForTest( final double cost ) { this.cost = cost; }

		@Override public PhysicalOperator getRootOperator() { return new DummyPhysicalOperatorForTest(); }
		@Override public ExpectedVariables getExpectedVariables() { return null; }
		@Override public int numberOfSubPlans() { return 0; }
		@Override public PhysicalPlan getSubPlan(int i) { return null; }
	}

	protected static class DummyRuleApplicationForTest implements RuleApplication {
		protected final PhysicalPlan resultingPlan;
		public DummyRuleApplicationForTest( final PhysicalPlan resultingPlan ) { this.resultingPlan = resultingPlan; }

		@Override public double getWeight() { return 0; }
		@Override public RewritingRule getRule() { return null; }
		@Override public PhysicalPlan getPlan() { return null; }
		@Override public PhysicalPlan getResultingPlan() { return resultingPlan; }
	}

}
