package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementProducer;

public interface NaryExecutableOp extends ExecutableOperator,
                                          IntermediateResultElementProducer
{
	// TODO define this interface

	@Override
	ExecutableOperatorStats getStats();
}
