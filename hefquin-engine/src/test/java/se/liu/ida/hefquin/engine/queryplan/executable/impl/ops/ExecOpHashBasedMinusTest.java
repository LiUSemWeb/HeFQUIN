package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpHashBasedMinusTest
{
	@Test
	public void subtract() throws ExecutionException {
		// Check that the operator correctly subtracts solution
		// mappings from the left-hand side.

		// Set up
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node y3 = NodeFactory.createURI("http://example.org/y3");

		final List<SolutionMapping> input1 = List.of(
			SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1),
			// Remaining solution mapping after the subtraction.
			SolutionMappingUtils.createSolutionMapping(var1, x2, var2, y2)
		);

		final List<SolutionMapping> input2 = List.of(
			SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1)
		);

		final Set<Var> varsCertain1 = Set.of(var1, var2);
		final Set<Var> varsPossible1 = Set.of();
		final Set<Var> varsCertain2 = Set.of(var1, var2);
		final Set<Var> varsPossible2 = Set.of();

		final ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		// Test
		final Iterator<SolutionMapping> it = runTest(input1, input2, false, inputVars);

		// Check
		assertTrue( it.hasNext() );
		final SolutionMapping sm = it.next();
		assertEquals( x2, sm.asJenaBinding().get(var1) );
		assertEquals( y2, sm.asJenaBinding().get(var2) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void emptySubtractionResult() throws ExecutionException {
		// Check that the operator correctly produces an empty
		// result if all solution mappings from the left-hand
		// side are subtracted.

		// Set up
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");

		final List<SolutionMapping> input1 = List.of(
			SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1)
		);

		final List<SolutionMapping> input2 = List.of(
			SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1)
		);

		final Set<Var> varsCertain1 = Set.of(var1, var2);
		final Set<Var> varsPossible1 = Set.of();
		final Set<Var> varsCertain2 = Set.of(var1, var2);
		final Set<Var> varsPossible2 = Set.of();

		ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		// Test
		final Iterator<SolutionMapping> it = runTest(input1, input2, false, inputVars);

		// Check
		assertFalse( it.hasNext() );
	}

	@Test
	public void oneCommonVariable() throws ExecutionException {
		// Test where the two inputs have only one common variable,
		// which is sufficient to make the solution mapping from
		// the left-hand side be subtracted.

		// Set up
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");

		final List<SolutionMapping> input1 = List.of(
			SolutionMappingUtils.createSolutionMapping(var1, x1, var2, y1)
		);

		final List<SolutionMapping> input2 = List.of(
			SolutionMappingUtils.createSolutionMapping(var1, x1, var3, y1)
		);

		final Set<Var> varsCertain1 = Set.of(var1, var2);
		final Set<Var> varsPossible1 = Set.of();
		final Set<Var> varsCertain2 = Set.of(var1, var3);
		final Set<Var> varsPossible2 = Set.of();

		ExpectedVariables[] inputVars = getExpectedVariables(varsCertain1, varsPossible1, varsCertain2, varsPossible2);

		// Test
		final Iterator<SolutionMapping> it = runTest(input1, input2, false, inputVars);

		// Check
		assertFalse( it.hasNext() );
	}

	/**
	 * Sends second input first.
	 */
	protected Iterator<SolutionMapping> runTest(
			final List<SolutionMapping> input1,
			final List<SolutionMapping> input2,
			final boolean sendAllSolMapsSeparately,
			final ExpectedVariables... inputVars ) throws ExecutionException
	{
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final ExecOpHashBasedMinus op = new ExecOpHashBasedMinus(sendAllSolMapsSeparately, inputVars[0], inputVars[1], sendAllSolMapsSeparately, null);

		if ( sendAllSolMapsSeparately == true ) {
			for ( final SolutionMapping sm : input2 ) {
				op.processInputFromChild2(sm, sink, null);
			}
		}
		else {
			op.processInputFromChild2(input2, sink, null);
		}

		op.wrapUpForChild2(sink, null);

		if ( sendAllSolMapsSeparately == true ) {
			for ( final SolutionMapping sm : input1 ) {
				op.processInputFromChild1(sm, sink, null);
			}
		}
		else {
			op.processInputFromChild1(input1, sink, null);
		}

		op.wrapUpForChild1(sink, null);

		return sink.getCollectedSolutionMappings().iterator();
	}

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

	protected ExecOpHashBasedMinus createExecOpForTest(
			final boolean useOuterJoinSemantics,
			final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

		return new ExecOpHashBasedMinus( false,            // mayReduce
		                                 inputVars[0], inputVars[1],
		                                 false,    // collectExceptions
		                                 null );              // qpInfo
	}

}
