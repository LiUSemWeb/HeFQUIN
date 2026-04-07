package se.liu.ida.hefquin.federation.members.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint.DataConversionException;
import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.exprs.MappingExpression;
import se.liu.ida.hefquin.mappings.algebra.exprs.MappingExpressionImpl;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtend;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpUnion;
import se.liu.ida.hefquin.mappings.algebra.ops.extexprs.ExtendExprConstant;
import se.liu.ida.hefquin.mappings.algebra.ops.extexprs.ExtendExprFunction;
import se.liu.ida.hefquin.mappings.algebra.ops.extexprs.ExtendExpression;
import se.liu.ida.hefquin.mappings.algebra.ops.extfcts.ExtnFct_ToBNode;
import se.liu.ida.hefquin.mappings.sources.SourceReference;
import se.liu.ida.hefquin.mappings.sources.json.JsonPathQuery;
import se.liu.ida.hefquin.mappings.sources.json.MappingOpExtractJSON;

public class WrappedRESTEndpointImplTest
{
	@Test
	public void evaluatePatternOverRDFView() throws DataConversionException {
		final String data = "{ \"current\": { \"temperature_2m\": 2.3, \"wind_speed_10m\": 1.0 } }";
		final String query =
				  "SELECT * WHERE {"
				+ " ?x <http://example.org/temperature> ?t ;"
				+ "    <http://example.org/windSpeed> ?w ."
				+ "}";

		final Element el = QueryFactory.create(query).getQueryPattern();
		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1(el);
		final WrappedRESTEndpoint ep = createEndpointForTests();

		final List<SolutionMapping> r = ep.evaluatePatternOverRDFView(pattern, data);

		assertNotNull( r );
		assertEquals( 1, r.size() );

		final Binding sm = r.get(0).asJenaBinding();

		final Var x = Var.alloc("x");
		final Var t = Var.alloc("t");
		final Var w = Var.alloc("w");

		assertTrue( sm.contains(x) );
		assertTrue( sm.contains(t) );
		assertTrue( sm.contains(w) );

		assertTrue( sm.get(x).isBlank() );
		assertTrue( sm.get(t).isLiteral() );
		assertTrue( sm.get(w).isLiteral() );

		final Object tValue = sm.get(t).getLiteral().getValue();
		final Object wValue = sm.get(w).getLiteral().getValue();

		assertTrue( tValue instanceof Double );
		assertTrue( wValue instanceof Double );
		assertEquals( 2.3, tValue );
		assertEquals( 1.0, wValue );
	}

	protected WrappedRESTEndpoint createEndpointForTests() {
		return new WrappedRESTEndpointImpl( "http://example.org/",
		                                    null,
		                                    createMappingExpressionForTests() );
	}

	protected MappingExpression createMappingExpressionForTests() {
		final SourceReference sr = new SourceReference() {};
		final JsonPathQuery query = new JsonPathQuery("$.current");

		final Map<String, JsonPathQuery> P1 = new HashMap<>();
		final Map<String, JsonPathQuery> P2 = new HashMap<>();
		P1.put( MappingRelation.oAttr, new JsonPathQuery("temperature_2m") );
		P2.put( MappingRelation.oAttr, new JsonPathQuery("wind_speed_10m") );

		final MappingOperator op1 = new MappingOpExtractJSON(sr, query, P1);
		final MappingOperator op2 = new MappingOpExtractJSON(sr, query, P2);
		final MappingExpression expr1 = new MappingExpressionImpl(op1);
		final MappingExpression expr2 = new MappingExpressionImpl(op2);

		final Node bnodeLabel = NodeFactory.createLiteralString("b");
		final ExtendExpression expr = new ExtendExprFunction( ExtnFct_ToBNode.instance,
		                                                      new ExtendExprConstant(bnodeLabel) );
		final MappingOperator op1S = new MappingOpExtend(op1, expr, MappingRelation.sAttr);
		final MappingOperator op2S = new MappingOpExtend(op2, expr, MappingRelation.sAttr);
		final MappingExpression expr1S = new MappingExpressionImpl(op1S, expr1);
		final MappingExpression expr2S = new MappingExpressionImpl(op2S, expr2);

		final Node uri1 = NodeFactory.createURI("http://example.org/temperature");
		final Node uri2 = NodeFactory.createURI("http://example.org/windSpeed");
		final ExtendExpression extExpr1 = new ExtendExprConstant(uri1);
		final ExtendExpression extExpr2 = new ExtendExprConstant(uri2);
		final MappingOperator op1P = new MappingOpExtend(op1S, extExpr1, MappingRelation.pAttr);
		final MappingOperator op2P = new MappingOpExtend(op2S, extExpr2, MappingRelation.pAttr);
		final MappingExpression expr1P = new MappingExpressionImpl(op1P, expr1S);
		final MappingExpression expr2P = new MappingExpressionImpl(op2P, expr2S);

		final MappingOperator op = new MappingOpUnion(op1P, op2P);
		return new MappingExpressionImpl(op, expr1P, expr2P);
	}

}
