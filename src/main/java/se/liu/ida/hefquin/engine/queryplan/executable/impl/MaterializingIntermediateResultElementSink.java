package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;

/**
 * An implementation of {@link IntermediateResultElementSink}
 * that collects all solution mappings that are sent to it. The
 * collected solution mappings can then be accessed by calling
 * {@link #getCollectedSolutionMappings()}.
 *
 * Attention, this implementation is not thread safe.
 */
public class MaterializingIntermediateResultElementSink
                             implements IntermediateResultElementSink
{
	protected final List<SolutionMapping> l = new ArrayList<>();

	@Override
	public void send( final SolutionMapping element ) {
		l.add(element);
	}

	/**
	 * Returns an iterable over the solution mappings that have been
	 * collected in this sink since the last time it was cleared (by
	 * calling {@link #clear()}) or, if it has not been cleared so
	 * far, since it was created.
	 */
	public Iterable<SolutionMapping> getCollectedSolutionMappings() {
		return l;
	}

	/**
	 * Clears this sink by removing all solution mappings that have
	 * so far been collected in this sink.
	 */
	public void clear() {
		l.clear();
	}
}
