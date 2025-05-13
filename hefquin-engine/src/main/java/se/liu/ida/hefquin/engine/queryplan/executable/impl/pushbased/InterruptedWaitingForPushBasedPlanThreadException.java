package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

/**
 * This class wraps an {@link InterruptedException} caught in a thread that
 * is waiting to consume the output of a {@link PushBasedPlanThread}.
 */
public class InterruptedWaitingForPushBasedPlanThreadException extends ConsumingPushBasedPlanThreadException
{
	private static final long serialVersionUID = -8330416567179466442L;

	public InterruptedWaitingForPushBasedPlanThreadException( final String msg,
	                                                          final InterruptedException e ) {
		super(msg, e);
	}

}
