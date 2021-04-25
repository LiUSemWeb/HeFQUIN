package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.EngineTestBase;
import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;

public abstract class ExecOpTestBase extends EngineTestBase
{
	protected static class IntermediateResultElementSinkForTest implements IntermediateResultElementSink
	{
		protected final List<SolutionMapping> l = new ArrayList<>();

		@Override
		public void send(SolutionMapping element) {
			l.add(element);
		}

		public Iterator<SolutionMapping> getSolMapsIter() {
			return l.iterator();
		}
	}

}
