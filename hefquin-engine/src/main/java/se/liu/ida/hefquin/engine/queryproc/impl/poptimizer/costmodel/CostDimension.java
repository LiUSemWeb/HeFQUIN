package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel;

public class CostDimension
{
	public final double weight;
	public final CostFunctionForPlan costFct;

	public CostDimension( final double weight, final CostFunctionForPlan costFct ) {
		assert costFct != null;

		this.weight = weight;
		this.costFct = costFct;
	}
}
