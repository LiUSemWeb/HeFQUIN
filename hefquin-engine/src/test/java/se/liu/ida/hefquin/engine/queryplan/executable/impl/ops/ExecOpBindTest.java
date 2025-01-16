package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;

public class ExecOpBindTest
{
	@Test
	public void extendTwoSolMaps() throws ExecOpExecutionException {
		final Node lit8 = NodeFactory.createLiteral( "8", XSDDatatype.XSDinteger );
		final Node lit2 = NodeFactory.createLiteral( "2", XSDDatatype.XSDinteger );
		final Var v1 = Var.alloc("v1");

		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
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

}
