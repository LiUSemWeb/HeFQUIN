package se.liu.ida.hefquin.federation.members.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

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
		final WrappedRESTEndpoint ep = new WrappedRESTEndpointImpl("http://example.org/", null);

		final List<SolutionMapping> result = ep.evaluatePatternOverRDFView(pattern, data);

		assertNotNull( result );
		assertEquals( 1, result.size() );

		final Binding sm = result.get(0).asJenaBinding();

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

}
