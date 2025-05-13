package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

/**
 * This class captures exceptions that may occur while trying
 * to consume the output of a {@link PushBasedPlanThread}.
 */
public class ConsumingPushBasedPlanThreadException extends ExecutionException
{
	private static final long serialVersionUID = -5963584792306532321L;

	public ConsumingPushBasedPlanThreadException( final String message,
	                                              final Throwable cause ) {
		super(message, cause);
	}

	public ConsumingPushBasedPlanThreadException( final String message ) {
		super(message);
	}

}
