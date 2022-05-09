package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * This is an iterable of solution mappings that enumerates the result of
 * joining two collections of solution mappings. The implementation uses
 * a {@link JoiningIteratorForSolMaps}.
 */
public class JoiningIterableForSolMaps implements Iterable<SolutionMapping>
{
	protected final Iterable<SolutionMapping> input1;
	protected final Iterable<SolutionMapping> input2;

	public JoiningIterableForSolMaps( final Iterable<SolutionMapping> input1,
	                                  final Iterable<SolutionMapping> input2 ) {
		assert input1 != null;
		assert input2 != null;

		this.input1 = input1;
		this.input2 = input2;
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
		return new JoiningIteratorForSolMaps(input1, input2);
	}

}
