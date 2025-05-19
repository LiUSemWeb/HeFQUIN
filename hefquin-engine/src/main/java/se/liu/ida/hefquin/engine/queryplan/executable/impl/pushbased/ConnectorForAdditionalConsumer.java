package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ConnectorForAdditionalConsumer extends PushBasedPlanThreadImplBase
{
	protected ConnectorForAdditionalConsumer( final ExecutionContext execCxt ) {
		super(execCxt);
	}

	@Override
	protected ExecutableOperator getExecOp() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void produceOutput( final IntermediateResultElementSink sink ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PushBasedPlanThread addConnectorForAdditionalConsumer() {
		throw new UnsupportedOperationException();
	}

}
