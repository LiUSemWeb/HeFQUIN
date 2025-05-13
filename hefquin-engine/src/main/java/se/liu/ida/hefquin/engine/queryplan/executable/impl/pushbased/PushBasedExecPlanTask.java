package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

public interface PushBasedExecPlanTask extends ExecPlanTask
{
	ExecPlanTask addConnectorForAdditionalConsumer();
}
