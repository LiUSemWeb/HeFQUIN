package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public interface PhysicalOptimizer
{
	boolean assumesLogicalMultiwayJoins();

	Pair<PhysicalPlan, PhysicalOptimizationStats> optimize( final LogicalPlan initialPlan ) throws PhysicalOptimizationException;
}
