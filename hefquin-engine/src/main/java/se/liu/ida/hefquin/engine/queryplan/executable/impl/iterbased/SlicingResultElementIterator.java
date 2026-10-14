package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public class SlicingResultElementIterator implements ResultElementIterator
{
	protected final ResultElementIterator input;
	protected final int offset;
	protected final int limit;

	protected boolean offsetConsumed = false;
	protected int outputCounter = 0;
	protected SolutionMapping nextSolMap = null;
	protected boolean done = false;

	public SlicingResultElementIterator( final ResultElementIterator input,
	                                     final int offset,
	                                     final int limit ) {
		assert input != null;
		assert offset >= 0;

		this.input = input;
		this.offset = offset;
		this.limit = limit;
	}

	@Override
	public boolean hasNext() throws ResultElementIterException {
		if ( done )
			return false;

		if ( offsetConsumed == false && offset > 0 ) {
			int offsetCounter = 0;

			// Consume x input solution mappings (if there are
			// that many), with x being the value of offset.
			while ( offsetCounter < offset && input.hasNext() ) {
				offsetCounter++;
				input.next();
			}

			offsetConsumed = true;

			// Check whether there have been x input solution mappings.
			if ( offsetCounter < offset ) {
				done = true;
				return false;
			}
		}

		if ( ! input.hasNext() ) {
			done = true;
			return false;
		}

		// If there is a limit, check whether we have reached that
		// limit with the previously returned solution mapping.
		if ( limit >= 0 && outputCounter == limit ) {
			done = true;
			return false;
		}

		return true;
	}

	@Override
	public SolutionMapping next() throws ResultElementIterException {
		if ( done )
			throw new NoSuchElementException();

		outputCounter++;
		return input.next();
	}

}
