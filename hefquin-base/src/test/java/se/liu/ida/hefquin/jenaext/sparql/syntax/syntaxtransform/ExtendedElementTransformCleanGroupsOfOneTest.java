package se.liu.ida.hefquin.jenaext.sparql.syntax.syntaxtransform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
import org.junit.Test;

import se.liu.ida.hefquin.jenaext.sparql.syntax.ElementServiceWithParams;
import se.liu.ida.hefquin.jenaext.sparql.syntax.ElementServiceWithValues;

public class ExtendedElementTransformCleanGroupsOfOneTest
{
	@Test
	public void transformSingletonGroupWithElementServiceWithParams() {
		// set up
		final ElementTriplesBlock bgp = new ElementTriplesBlock();
		bgp.addTriple( Triple.create(Var.alloc("s"),
		                             Var.alloc("p"),
		                             Var.alloc("o")) );

		final ElementGroup innerGroup1 = new ElementGroup();
		innerGroup1.addElement(bgp);

		final ElementGroup innerGroup2 = new ElementGroup();
		innerGroup2.addElement(innerGroup1);

		final ElementServiceWithParams elmtService = new ElementServiceWithParams(
				NodeFactory.createURI("http://example.org"),
				innerGroup2,
				false, // no SILENT
				Map.of("param", Var.alloc("v")) );

		final ElementGroup outerGroup = new ElementGroup();
		outerGroup.addElement(elmtService);

		// run
		final ElementTransform t = new ExtendedElementTransformCleanGroupsOfOne();
		final Element result = ElementTransformer.transform(outerGroup, t);

		// check
		assertTrue( result instanceof ElementServiceWithParams );
		assertEquals( ElementServiceWithParams.class, result.getClass() );

		final ElementServiceWithParams s = (ElementServiceWithParams) result;
		assertEquals( elmtService.getServiceNode(), s.getServiceNode() );
		assertEquals( elmtService.getSilent(),      s.getSilent() );
		assertEquals( elmtService.getParamVars(),   s.getParamVars() );

		final Element subElmt = s.getElement();
		assertEquals( ElementGroup.class, subElmt.getClass() );
		assertEquals( innerGroup1, subElmt );
	}

	@Test
	public void transformSingletonGroupWithElementServiceWithValues() {
		// set up
		final ElementTriplesBlock bgp = new ElementTriplesBlock();
		bgp.addTriple( Triple.create(Var.alloc("s"),
		                             Var.alloc("p"),
		                             Var.alloc("o")) );

		final ElementGroup innerGroup1 = new ElementGroup();
		innerGroup1.addElement(bgp);

		final ElementGroup innerGroup2 = new ElementGroup();
		innerGroup2.addElement(innerGroup1);

		final ElementServiceWithValues elmtService = new ElementServiceWithValues(
				Var.alloc("s"),
				innerGroup2,
				false, // no SILENT
				Set.of(NodeFactory.createURI("http://example.org")) );

		final ElementGroup outerGroup = new ElementGroup();
		outerGroup.addElement(elmtService);

		// run
		final ElementTransform t = new ExtendedElementTransformCleanGroupsOfOne();
		final Element result = ElementTransformer.transform(outerGroup, t);

		// check
		assertTrue( result instanceof ElementServiceWithValues );
		assertEquals( ElementServiceWithValues.class, result.getClass() );

		final ElementServiceWithValues s = (ElementServiceWithValues) result;
		assertEquals( elmtService.getServiceNode(),    s.getServiceNode() );
		assertEquals( elmtService.getSilent(),         s.getSilent() );
		assertEquals( elmtService.getPossibleValues(), s.getPossibleValues() );

		final Element subElmt = s.getElement();
		assertEquals( ElementGroup.class, subElmt.getClass() );
		assertEquals( innerGroup1, subElmt );
	}
}
