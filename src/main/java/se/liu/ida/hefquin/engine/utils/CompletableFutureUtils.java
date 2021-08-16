package se.liu.ida.hefquin.engine.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureUtils
{
	public static Object[] getAll( final CompletableFuture<?>[] futures )
			throws GetAllException
	{
		final Object[] result = new Object[futures.length];

		GetAllException ex = null;

		for ( int i = 0; i < futures.length; ++i ) {
			if ( ex == null ) {
				try {
					result[i] = futures[i].get();
				}
				catch ( final InterruptedException | ExecutionException e ) {
					ex = new GetAllException(e,i);
				}
			}
			else {
				futures[i].cancel(true);
			}
		}

		if ( ex != null ) {
			throw ex;
		}

		return result;
	}


	public static class GetAllException extends Exception {
		private static final long serialVersionUID = 3973031032404035315L;

		public final int i;

		public GetAllException( final Throwable cause, final int i ) {
			super(cause);
			this.i = i;
		}
	}

}
