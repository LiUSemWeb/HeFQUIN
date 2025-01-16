package se.liu.ida.hefquin.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCleanGroupsOfOne;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
import org.junit.Test;

public class ValuesServiceQueryResolverTest
{
	@Test
	public void testIsQueryToBeExpanded_Yes1() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES ?s { <http://example.org> }"
				+ " SERVICE ?s { ?x a ?y }"
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(true, r);
	}

	@Test
	public void testIsQueryToBeExpanded_Yes2() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> <http://example2.org>) }"
				+ " SERVICE ?s1 { ?x a ?y }"
				+ " SERVICE ?s2 { ?x a ?y }"
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(true, r);
	}

	@Test
	public void testIsQueryToBeExpanded_Yes3() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> <http://example2.org>) }"
				+ " SERVICE ?s1 { ?x a ?y }"
				+ " SERVICE ?s2 { ?x a ?y }"
				+ " SERVICE ?s1 { ?x a ?y }"  // yes, the same
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(true, r);
	}

	@Test
	public void testIsQueryToBeExpanded_Yes4() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> <http://example2.org>) }"
				+ " SERVICE ?s1 { ?x a ?y }"  // yes, ?s2 is not mentioned
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(true, r);
	}

	@Test
	public void testIsQueryToBeExpanded_Yes5() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> UNDEF) }"
				+ " SERVICE ?s1 { ?x a ?y }"
				+ " SERVICE ?s2 { ?x a ?y }" // yes, any of the SERVICE variables may be unbound
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(true, r);
	}

	@Test
	public void testIsQueryToBeExpanded_Yes6() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES ?s { <http://example.org> }"
				+ " SERVICE ?s { ?x a ?y }"
				+ " SERVICE <http://example2.org> { ?x a ?y }"
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(true, r);
	}

	@Test
	public void testIsQueryToBeExpanded_No1() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " ?x a ?y ." // no VALUES or SERVICE whatsoever
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(false, r);
	}

	@Test
	public void testIsQueryToBeExpanded_No2() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES ?s { <http://example.org> }"
				+ " ?x a ?y ." // VALUES clause without SERVICE clause
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(false, r);
	}

	@Test
	public void testIsQueryToBeExpanded_No3() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " SERVICE ?s1 { ?x a ?y }" // SERVICE clauses without VALUES clause
				+ " SERVICE ?s2 { ?x a ?y }"
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(false, r);
	}

	@Test
	public void testIsQueryToBeExpanded_No4() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES ?s1 { <http://example.org> }"
				+ " SERVICE ?s2 { ?x a ?y }"
				+ " ?x a ?y ."  // not only VALUES and SERVICE clauses
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(false, r);
	}

	@Test
	public void testIsQueryToBeExpanded_No5() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES ?s1 { <http://example.org> }"
				+ " SERVICE ?s2 { ?x a ?y }" // SERVICE variable must be in VALUES clause
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(false, r);
	}

	@Test
	public void testIsQueryToBeExpanded_No6() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES ?s { <http://example.org> }"
				+ " SERVICE ?s { ?s a ?y }" // SERVICE variable must not be used inside SERVICE clause
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(false, r);
	}

	@Test
	public void testIsQueryToBeExpanded_No7() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s ?a) { (<http://example.org> <http://example.org>) }"
				+ " SERVICE ?s { ?x a ?y }"
				+ " FILTER( ?a = <http://example.com> )" // VALUES variables must not be used anywhere
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final boolean r = ValuesServiceQueryResolver.isQueryToBeExpanded(q);
		assertEquals(false, r);
	}

	@Test
	public void testExpandValuesPlusServicePattern1() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> <http://example2.org>) }"
				+ " SERVICE ?s1 { ?x a ?y }"
				+ " SERVICE ?s9 { ?x a ?y }" // incorrect pattern! --> no change to the query
				+ "}";
		final Query q = QueryFactory.create(qStr);
		final Element e = q.getQueryPattern();

		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);

		assertTrue( q.getQueryPattern() == e );
		assertTrue( q.getQueryPattern().equals(e) );
	}

	@Test
	public void testExpandValuesPlusServicePattern2() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> <http://example2.org>) }"
				+ " SERVICE ?s1 { ?x a ?y }"
				+ " SERVICE ?s2 { ?x a ?y }"
				+ "}";
		final Query q = QueryFactory.create(qStr);
		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);

		final String expectedQueryString =
				  "SELECT * WHERE {"
				+ " SERVICE <http://example1.org> { ?x a ?y }"
				+ " SERVICE <http://example2.org> { ?x a ?y }"
				+ " BIND( <http://example1.org> AS ?s1 )"
				+ " BIND( <http://example2.org> AS ?s2 )"
				+ "}";
		final Element expectedPattern = QueryFactory.create(expectedQueryString).getQueryPattern();

		assertTrue( q.getQueryPattern().equals(expectedPattern) );
	}

	@Test
	public void testExpandValuesPlusServicePattern3() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> <http://example2.org>) }"
				+ " SERVICE ?s1 { ?x a ?y }"
				+ " SERVICE ?s1 { ?x a ?y }" // same variable!
				+ "}";
		final Query q = QueryFactory.create(qStr);
		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);

		final String expectedQueryString =
				  "SELECT * WHERE {"
				+ " SERVICE <http://example1.org> { ?x a ?y }"
				+ " SERVICE <http://example1.org> { ?x a ?y }"
				+ " BIND( <http://example1.org> AS ?s1 )"
				+ " BIND( <http://example2.org> AS ?s2 )"   // needs to be added even if not used
				+ "}";
		final Element expectedPattern = QueryFactory.create(expectedQueryString).getQueryPattern();

		assertTrue( q.getQueryPattern().equals(expectedPattern) );
	}

	@Test
	public void testExpandValuesPlusServicePattern4() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> <http://example2.org>) (<http://example3.org> <http://example4.org>) }"
				+ " SERVICE ?s1 { ?x a ?y }"
				+ " SERVICE ?s2 { ?z a ?y }"
				+ "}";
		final Query q = QueryFactory.create(qStr);
		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);

		final String expectedQueryString =
				  "SELECT * WHERE {"
				+ " {"
				+ "   SERVICE <http://example1.org> { ?x a ?y }"
				+ "   SERVICE <http://example2.org> { ?z a ?y }"
				+ "   BIND( <http://example1.org> AS ?s1 )"
				+ "   BIND( <http://example2.org> AS ?s2 )"
				+ " } UNION {"
				+ "   SERVICE <http://example3.org> { ?x a ?y }"
				+ "   SERVICE <http://example4.org> { ?z a ?y }"
				+ "   BIND( <http://example3.org> AS ?s1 )"
				+ "   BIND( <http://example4.org> AS ?s2 )"
				+ " }"
				+ "}";
		final Element expectedPattern = QueryFactory.create(expectedQueryString).getQueryPattern();

		final ElementTransform t = new ElementTransformCleanGroupsOfOne();
		final Element expectedPattern2 = ElementTransformer.transform(expectedPattern, t);

		assertTrue( q.getQueryPattern().equals(expectedPattern2) );
	}

	@Test
	public void testExpandValuesPlusServicePattern5() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES ?s { <http://example1.org> <http://example2.org> }"
				+ " SERVICE ?s { ?x a ?y }"
				+ "}";
		final Query q = QueryFactory.create(qStr);
		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);

		final String expectedQueryString =
				  "SELECT * WHERE {"
				+ " {"
				+ "   SERVICE <http://example1.org> { ?x a ?y }"
				+ "   BIND( <http://example1.org> AS ?s )"
				+ " } UNION {"
				+ "   SERVICE <http://example2.org> { ?x a ?y }"
				+ "   BIND( <http://example2.org> AS ?s )"
				+ " }"
				+ "}";
		final Element expectedPattern = QueryFactory.create(expectedQueryString).getQueryPattern();

		final ElementTransform t = new ElementTransformCleanGroupsOfOne();
		final Element expectedPattern2 = ElementTransformer.transform(expectedPattern, t);

		assertTrue( q.getQueryPattern().equals(expectedPattern2) );
	}

	@Test
	public void testExpandValuesPlusServicePattern6() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> UNDEF) }"
				+ " SERVICE ?s1 { ?x1 a ?y }"
				+ " SERVICE ?s2 { ?x2 a ?y }"
				+ "}";
		final Query q = QueryFactory.create(qStr);
		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);

		final String expectedQueryString =
				  "SELECT * WHERE {"
				+ " SERVICE <http://example1.org> { ?x1 a ?y }"
				+ " BIND( <http://example1.org> AS ?s1 )"
				+ "}";
		final Element expectedPattern = QueryFactory.create(expectedQueryString).getQueryPattern();

		assertTrue( q.getQueryPattern().equals(expectedPattern) );
	}

	@Test
	public void testExpandValuesPlusServicePattern7() {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> UNDEF) (UNDEF <http://example4.org>) }"
				+ " SERVICE ?s1 { ?x a ?y }"
				+ " SERVICE ?s2 { ?z a ?y }"
				+ "}";
		final Query q = QueryFactory.create(qStr);
		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);

		final String expectedQueryString =
				  "SELECT * WHERE {"
				+ " {"
				+ "   SERVICE <http://example1.org> { ?x a ?y }"
				+ "   BIND( <http://example1.org> AS ?s1 )"
				+ " } UNION {"
				+ "   SERVICE <http://example4.org> { ?z a ?y }"
				+ "   BIND( <http://example4.org> AS ?s2 )"
				+ " }"
				+ "}";
		final Element expectedPattern = QueryFactory.create(expectedQueryString).getQueryPattern();

		final ElementTransform t = new ElementTransformCleanGroupsOfOne();
		final Element expectedPattern2 = ElementTransformer.transform(expectedPattern, t);

		assertTrue( q.getQueryPattern().equals(expectedPattern2) );
	}

}
