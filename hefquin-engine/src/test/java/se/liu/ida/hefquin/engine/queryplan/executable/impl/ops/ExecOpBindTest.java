package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;

public class ExecOpBindTest
{
	@Test
	public void extendTwoSolMapsSeparate() throws ExecOpExecutionException {
		final Node lit8 = NodeFactory.createLiteral( "8", XSDDatatype.XSDinteger );
		final Node lit2 = NodeFactory.createLiteral( "2", XSDDatatype.XSDinteger );
		final Var v1 = Var.alloc("v1");

		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(v1, lit8);
		final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(v1, lit2);

		final Var v2 = Var.alloc("v2");
		final Expr addOne = ExprUtils.parse("?v1 + 1");
		final ExecOpBind op = new ExecOpBind(v2, addOne, false);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		op.process(sm1, sink, null);
		op.process(sm2, sink, null);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );
		final Node lit9 = NodeFactory.createLiteral( "9", XSDDatatype.XSDinteger );
		assertEquals( lit9, it.next().asJenaBinding().get(v2) );

		assertTrue( it.hasNext() );
		final Node lit3 = NodeFactory.createLiteral( "3", XSDDatatype.XSDinteger );
		assertEquals( lit3, it.next().asJenaBinding().get(v2) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void extendTwoSolMapsTogether() throws ExecOpExecutionException {
		final Node lit8 = NodeFactory.createLiteral( "8", XSDDatatype.XSDinteger );
		final Node lit2 = NodeFactory.createLiteral( "2", XSDDatatype.XSDinteger );
		final Var v1 = Var.alloc("v1");

		final List<SolutionMapping> input = new ArrayList<>(2);
		input.add( SolutionMappingUtils.createSolutionMapping(v1, lit8) );
		input.add( SolutionMappingUtils.createSolutionMapping(v1, lit2) );

		final Var v2 = Var.alloc("v2");
		final Expr addOne = ExprUtils.parse("?v1 + 1");
		final ExecOpBind op = new ExecOpBind(v2, addOne, false);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		op.process(input, sink, null);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );
		final Node lit9 = NodeFactory.createLiteral( "9", XSDDatatype.XSDinteger );
		assertEquals( lit9, it.next().asJenaBinding().get(v2) );

		assertTrue( it.hasNext() );
		final Node lit3 = NodeFactory.createLiteral( "3", XSDDatatype.XSDinteger );
		assertEquals( lit3, it.next().asJenaBinding().get(v2) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void extendTwoSolMapsSeparateWithOneFailing() throws ExecOpExecutionException {
		final Node lit8 = NodeFactory.createLiteral( "8", XSDDatatype.XSDinteger );
		final Node uri = NodeFactory.createURI( "http://example.org" );
		final Var v1 = Var.alloc("v1");

		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(v1, lit8);
		final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(v1, uri);

		final Var v2 = Var.alloc("v2");
		final Expr addOne = ExprUtils.parse("?v1 + 1"); //should fail for the URI
		final ExecOpBind op = new ExecOpBind(v2, addOne, false);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		op.process(sm1, sink, null);
		op.process(sm2, sink, null);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );
		final Node lit9 = NodeFactory.createLiteral( "9", XSDDatatype.XSDinteger );
		assertEquals( lit9, it.next().asJenaBinding().get(v2) );

		assertTrue( it.hasNext() );
		assertFalse( it.next().asJenaBinding().contains(v2) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void extendTwoSolMapsTogetherWithOneFailing() throws ExecOpExecutionException {
		final Node lit8 = NodeFactory.createLiteral( "8", XSDDatatype.XSDinteger );
		final Node uri = NodeFactory.createURI( "http://example.org" );
		final Var v1 = Var.alloc("v1");

		final List<SolutionMapping> input = new ArrayList<>(2);
		input.add( SolutionMappingUtils.createSolutionMapping(v1, lit8) );
		input.add( SolutionMappingUtils.createSolutionMapping(v1, uri) );

		final Var v2 = Var.alloc("v2");
		final Expr addOne = ExprUtils.parse("?v1 + 1"); //should fail for the URI
		final ExecOpBind op = new ExecOpBind(v2, addOne, false);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		op.process(input, sink, null);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );
		final Node lit9 = NodeFactory.createLiteral( "9", XSDDatatype.XSDinteger );
		assertEquals( lit9, it.next().asJenaBinding().get(v2) );

		assertTrue( it.hasNext() );
		assertFalse( it.next().asJenaBinding().contains(v2) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void extendSolMapWithSameVariable() throws ExecOpExecutionException {
		final Node lit8 = NodeFactory.createLiteral( "8", XSDDatatype.XSDinteger );
		final Var v1 = Var.alloc("v1");

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(v1, lit8);

		final Expr addOne = ExprUtils.parse("?v1 + 1");
		final ExecOpBind op = new ExecOpBind(v1, addOne, false);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		assertThrows( ExecOpExecutionException.class, () -> op.process(sm, sink, null) );
	}

	@Test
	public void extendTwoSolMapsWithTwoVariablesSeparate() throws ExecOpExecutionException {
		final Node lit8 = NodeFactory.createLiteral( "8", XSDDatatype.XSDinteger );
		final Node lit2 = NodeFactory.createLiteral( "2", XSDDatatype.XSDinteger );
		final Var v1 = Var.alloc("v1");

		final SolutionMapping inSM1 = SolutionMappingUtils.createSolutionMapping(v1, lit8);
		final SolutionMapping inSM2 = SolutionMappingUtils.createSolutionMapping(v1, lit2);

		final Var v2 = Var.alloc("v2");
		final Var v3 = Var.alloc("v3");
		final VarExprList veList = new VarExprList();
		veList.add( v2, ExprUtils.parse("?v1 + 1") );
		veList.add( v3, ExprUtils.parse("?v1 - 1") );

		final ExecOpBind op = new ExecOpBind(veList, false);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		op.process(inSM1, sink, null);
		op.process(inSM2, sink, null);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );
		final Binding sm1 = it.next().asJenaBinding();
		final Node lit9 = NodeFactory.createLiteral( "9", XSDDatatype.XSDinteger );
		final Node lit7 = NodeFactory.createLiteral( "7", XSDDatatype.XSDinteger );
		assertEquals( lit9, sm1.get(v2) );
		assertEquals( lit7, sm1.get(v3) );

		assertTrue( it.hasNext() );
		final Binding sm2 = it.next().asJenaBinding();
		final Node lit3 = NodeFactory.createLiteral( "3", XSDDatatype.XSDinteger );
		final Node lit1 = NodeFactory.createLiteral( "1", XSDDatatype.XSDinteger );
		assertEquals( lit3, sm2.get(v2) );
		assertEquals( lit1, sm2.get(v3) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void extendTwoSolMapsWithTwoVariablesTogether() throws ExecOpExecutionException {
		final Node lit8 = NodeFactory.createLiteral( "8", XSDDatatype.XSDinteger );
		final Node lit2 = NodeFactory.createLiteral( "2", XSDDatatype.XSDinteger );
		final Var v1 = Var.alloc("v1");

		final List<SolutionMapping> input = new ArrayList<>(2);
		input.add( SolutionMappingUtils.createSolutionMapping(v1, lit8) );
		input.add( SolutionMappingUtils.createSolutionMapping(v1, lit2) );

		final Var v2 = Var.alloc("v2");
		final Var v3 = Var.alloc("v3");
		final VarExprList veList = new VarExprList();
		veList.add( v2, ExprUtils.parse("?v1 + 1") );
		veList.add( v3, ExprUtils.parse("?v1 - 1") );

		final ExecOpBind op = new ExecOpBind(veList, false);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		op.process(input, sink, null);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );
		final Binding sm1 = it.next().asJenaBinding();
		final Node lit9 = NodeFactory.createLiteral( "9", XSDDatatype.XSDinteger );
		final Node lit7 = NodeFactory.createLiteral( "7", XSDDatatype.XSDinteger );
		assertEquals( lit9, sm1.get(v2) );
		assertEquals( lit7, sm1.get(v3) );

		assertTrue( it.hasNext() );
		final Binding sm2 = it.next().asJenaBinding();
		final Node lit3 = NodeFactory.createLiteral( "3", XSDDatatype.XSDinteger );
		final Node lit1 = NodeFactory.createLiteral( "1", XSDDatatype.XSDinteger );
		assertEquals( lit3, sm2.get(v2) );
		assertEquals( lit1, sm2.get(v3) );

		assertFalse( it.hasNext() );
	}

}
