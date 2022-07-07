package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;

/**
 * An implementation of {@link IntermediateResultElementSink}
 * that collects all solution mappings that are sent to it. The
 * collected solution mappings can then be accessed by calling
 * {@link #getMaterializedIntermediateResult()}.
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
	 * collected in this sink since it was created.
	 */
	public Iterable<SolutionMapping> getMaterializedIntermediateResult() {
		return l;
	}
}
