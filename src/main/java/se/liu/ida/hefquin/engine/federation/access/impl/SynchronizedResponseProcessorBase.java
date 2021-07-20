package se.liu.ida.hefquin.engine.federation.access.impl;

import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.engine.federation.access.ResponseProcessingException;
import se.liu.ida.hefquin.engine.federation.access.ResponseProcessor;

/**
 * This is an abstract base class for thread-safe implementations of
 * {@link ResponseProcessor} that are meant to process one response at
 * a time. After calling {@link #waitUntilResponseIsProcessed()}, such
 * a response processor can be used again for another response.
 */
public abstract class SynchronizedResponseProcessorBase<RespType extends DataRetrievalResponse>
            implements ResponseProcessor<RespType>
{
	private static class MyState {
		public boolean waitingForResponse = true;
		public boolean processingResponse = false;
		public boolean processingCompleted = false;
	}
	private MyState state = new MyState();

	@Override
	public final void process( final RespType response )
			throws ResponseProcessingException
	{
		synchronized (state) {
			if ( ! state.waitingForResponse ) {
				throw new ResponseProcessingException("Call to process a response even if this response processor (type: " + getClass().getName() + ") is not waiting for a response.", response);
			}
			if ( state.processingResponse ) {
				throw new ResponseProcessingException("Call to process a response even if this response processor (type: " + getClass().getName() + ") is still processing a response.", response);
			}
			if ( state.processingCompleted ) {
				throw new ResponseProcessingException("Call to process a response while it has not been checked whether this response processor (type: " + getClass().getName() + ") has completed the processing of the previous response.", response);
			}
			state.waitingForResponse = false;
			state.processingResponse = true;
		}

		_process(response);

		synchronized (state) {
			state.processingResponse = false;
			state.processingCompleted = true;
			state.notifyAll();
		}
	}

	public final void waitUntilResponseIsProcessed() throws InterruptedException {
		synchronized (state) {
			while ( ! state.processingCompleted ) {
				state.wait();
			}
			state.processingCompleted = false;
			state.waitingForResponse = true;
		}
	}

	protected abstract void _process( final RespType response ) throws ResponseProcessingException;
}
