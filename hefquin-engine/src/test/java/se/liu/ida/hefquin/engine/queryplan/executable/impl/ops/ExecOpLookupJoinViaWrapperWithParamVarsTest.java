package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;
import se.liu.ida.hefquin.federation.members.impl.WrappedRESTEndpointImpl;

public class ExecOpLookupJoinViaWrapperWithParamVarsTest extends ExecOpTestBase
{
	@Test
	public void paramVarsAreTheOnlyJoinVars() throws ExecutionException {
		final String query =
				  "SELECT * WHERE {"
				+ " ?x <http://example.org/temperature> ?t ;"
				+ "    <http://example.org/windSpeed> ?w ."
				+ "}";

		final Var v = Var.alloc("v");

		final UnaryExecutableOp op = createOperatorForTest( query, List.of(v) );

		final Node lit = NodeFactory.createLiteralDT( "2.3", XSDDatatype.XSDdouble );
		final SolutionMapping smIn = SolutionMappingUtils.createSolutionMapping(v, lit);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		final ExecutionContext cxt = getExecContextForTests(null);

		op.process(smIn, sink, cxt);
		op.concludeExecution(sink, cxt);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();
		assertTrue( it.hasNext() );

		final Binding smOut = it.next().asJenaBinding();
		assertEquals( 4, smOut.size() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void paramVarsAreNotTheOnlyJoinVars1() throws ExecutionException {
		// The difference to the previous test (paramVarsAreTheOnlyJoinVars)
		// is that the solution mapping that is passed as input to the operator
		// contains an additional binding, which is for one of the variables
		// that are also mentioned in the graph pattern of the operator. The
		// value bound to that variable is the same as the value that shall
		// be obtained for the pattern; hence, the query result should be the
		// same as in the previous test.

		final String query =
				  "SELECT * WHERE {"
				+ " ?x <http://example.org/temperature> ?t ;"
				+ "    <http://example.org/windSpeed> ?w ."
				+ "}";

		final Var v = Var.alloc("v");
		final Var t = Var.alloc("t");

		final UnaryExecutableOp op = createOperatorForTest( query, List.of(v) );

		final Node litForV = NodeFactory.createLiteralDT( "0.1", XSDDatatype.XSDdouble );
		final Node litForT = NodeFactory.createLiteralDT( "2.3", XSDDatatype.XSDdouble );
		final SolutionMapping smIn = SolutionMappingUtils.createSolutionMapping(
				v, litForV,
				t, litForT );

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		final ExecutionContext cxt = getExecContextForTests(null);

		op.process(smIn, sink, cxt);
		op.concludeExecution(sink, cxt);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();
		assertTrue( it.hasNext() );

		final Binding smOut = it.next().asJenaBinding();
		assertEquals( 4, smOut.size() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void paramVarsAreNotTheOnlyJoinVars2() throws ExecutionException {
		// The difference to the previous test (paramVarsAreNotTheOnlyJoinVars1)
		// is that the value that the input solution mapping has for the variable
		// that is also mentioned in the graph pattern of the operator is not
		// the same as the value that shall be obtained for the pattern; hence,
		// the input solution mapping is not compatible with the solution mapping
		// obtained for the pattern.

		final String query =
				  "SELECT * WHERE {"
				+ " ?x <http://example.org/temperature> ?t ;"
				+ "    <http://example.org/windSpeed> ?w ."
				+ "}";

		final Var v = Var.alloc("v");
		final Var t = Var.alloc("t");

		final UnaryExecutableOp op = createOperatorForTest( query, List.of(v) );

		final Node litForV = NodeFactory.createLiteralDT( "0.1", XSDDatatype.XSDdouble );
		final Node litForT = NodeFactory.createLiteralDT( "0.0", XSDDatatype.XSDdouble );
		final SolutionMapping smIn = SolutionMappingUtils.createSolutionMapping(
				v, litForV,
				t, litForT );

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		final ExecutionContext cxt = getExecContextForTests(null);

		op.process(smIn, sink, cxt);
		op.concludeExecution(sink, cxt);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();
		assertFalse( it.hasNext() );
	}


	// -------- helpers ----------

	protected UnaryExecutableOp createOperatorForTest( final String query,
	                                                   final List<Var> paramVarsOfEndpoint ) {
		assert paramVarsOfEndpoint.size() == 1;

		final Element el = QueryFactory.create(query).getQueryPattern();
		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1(el);

		final RESTEndpoint.Parameter param = new RESTEndpoint.Parameter() {
			@Override public String getName() { return "lat"; }
			@Override public RDFDatatype getType() { return XSDDatatype.XSDdouble; }
		};

		final WrappedRESTEndpoint ep = new WrappedRESTEndpointImpl("http://example.org/", List.of(param) );

		return new ExecOpLookupJoinViaWrapperWithParamVars( pattern,
		                                                    paramVarsOfEndpoint,
		                                                    ep,
		                                                    false,
		                                                    null );
	}
}
