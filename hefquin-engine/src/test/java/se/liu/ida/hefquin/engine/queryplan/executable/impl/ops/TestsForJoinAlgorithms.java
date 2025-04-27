package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

import java.util.Iterator;
import java.util.Set;

/**
 * This is an abstract class with tests for any algorithm that is
 * meant to be used as an implementation for the join operator.
 */
public abstract class TestsForJoinAlgorithms extends ExecOpTestBase
{
	protected ExpectedVariables[] getExpectedVariables(
			final Set<Var> varsCertain1,
			final Set<Var> varsPossible1,
			final Set<Var> varsCertain2,
			final Set<Var> varsPossible2)
	{
		final ExpectedVariables[] inputVars = new ExpectedVariables[2];
		inputVars[0] = new ExpectedVariables() {
			public Set<Var> getCertainVariables() { return varsCertain1;}
			public Set<Var> getPossibleVariables() { return varsPossible1;}
		};
		inputVars[1] = new ExpectedVariables() {
			public Set<Var> getCertainVariables() { return varsCertain2;}
			public Set<Var> getPossibleVariables() { return varsPossible2;}
		};
		return inputVars;
	}

	protected Iterator<SolutionMapping> runTest(
			final IntermediateResultBlock input1,
			final IntermediateResultBlock input2,
			final ExpectedVariables... inputVars ) throws ExecutionException
	{
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final BinaryExecutableOp op = createExecOpForTest(inputVars);

		op.processBlockFromChild1(input1, sink, null);
		op.wrapUpForChild1(sink, null);
		op.processBlockFromChild2(input2, sink, null);
		op.wrapUpForChild2(sink, null);

		return sink.getCollectedSolutionMappings().iterator();
	}

	protected abstract BinaryExecutableOp createExecOpForTest( final ExpectedVariables... inputVars );
}
