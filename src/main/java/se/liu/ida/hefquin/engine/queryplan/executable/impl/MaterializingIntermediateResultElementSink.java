package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class MaterializingIntermediateResultElementSink
                             implements ClosableIntermediateResultElementSink
{
	protected final List<SolutionMapping> l = new ArrayList<>();
	private boolean closed = false;

	@Override
	public void send(SolutionMapping element) {
		if ( ! closed ) {
			l.add(element);
		}
	}

	@Override
	public void close() {
		closed = true;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	public Iterable<SolutionMapping> getMaterializedIntermediateResult() {
		return l;
	}

}
