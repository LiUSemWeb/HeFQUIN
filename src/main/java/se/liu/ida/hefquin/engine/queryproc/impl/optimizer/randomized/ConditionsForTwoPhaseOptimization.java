package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.randomized;

public class ConditionsForTwoPhaseOptimization {
	protected StoppingConditionForIterativeImprovement conditionII;
	protected EquilibriumConditionForSimulatedAnnealing conditionSA;
	
	public ConditionsForTwoPhaseOptimization (final StoppingConditionForIterativeImprovement x, final EquilibriumConditionForSimulatedAnnealing y) {
		conditionII = x;
		conditionSA = y;
	}
	
	public StoppingConditionForIterativeImprovement getStopping() {
		return conditionII;
	}
	public EquilibriumConditionForSimulatedAnnealing getEquilibrium() {
		return conditionSA;
	}
}
