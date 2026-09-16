package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.datastructures.SolutionMappingsIndex;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;

/**
 * A thread-safe implementation of {@link IntermediateResultElementSink}
 * that puts all solution mappings that are sent to it into an index.
 * The index to be used needs to be provided when creating this sink.
 */
public class IndexingIntermediateResultElementSink
                      implements IntermediateResultElementSink
{
	protected final SolutionMappingsIndex idx;

	public IndexingIntermediateResultElementSink( final SolutionMappingsIndex idx ) {
		assert idx != null;
		this.idx = idx;
	}

	@Override
	public void send( final SolutionMapping sm ) {
		synchronized (idx) {
			idx.add(sm);
		}
	}
}
