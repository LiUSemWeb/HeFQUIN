package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.costmodel;

public class CostDimension
{
	public final int weight;
	public final CostFunctionForPlan costFct;

	public CostDimension( final int weight, final CostFunctionForPlan costFct ) {
		assert costFct != null;

		this.weight = weight;
		this.costFct = costFct;
	}
}
