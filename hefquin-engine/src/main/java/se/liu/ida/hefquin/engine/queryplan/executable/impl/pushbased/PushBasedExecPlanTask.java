package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecPlanTask;

public interface PushBasedExecPlanTask extends ExecPlanTask
{
	ExecPlanTask addConnectorForAdditionalConsumer( int preferredMinimumBlockSize );
}
