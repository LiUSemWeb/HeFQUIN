package se.liu.ida.hefquin.engine;

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
	@Test(expected = UnsupportedQueryException.class)
	public void testUnsupportedPattern1() throws UnsupportedQueryException, IllegalQueryException {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> <http://example2.org>) }"
				+ " SERVICE ?s1 { ?x a ?y }"
				+ " SERVICE ?s9 { ?x a ?y }" // unsupported pattern! --> exception
				+ "}";
		final Query q = QueryFactory.create(qStr);

		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);
	}

	@Test(expected = UnsupportedQueryException.class)
	public void testUnsupportedPattern2() throws UnsupportedQueryException, IllegalQueryException {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES ?s1 { <http://example1.org> }"
				+ " SERVICE ?s1 { ?x a ?y }"
				+ " VALUES ?s2 { <http://example2.org> }"
				+ " SERVICE ?s2 { ?x a ?y }"
				+ " SERVICE ?s1 { ?x a ?y }" // unsupported pattern! --> exception
				+ "}";
		final Query q = QueryFactory.create(qStr);

		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);
	}

	@Test(expected = IllegalQueryException.class)
	public void testIllegalPattern1() throws UnsupportedQueryException, IllegalQueryException {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES ?s1 { 'hello' }"   // literal for service variable
				+ " SERVICE ?s1 { ?x a ?y }"  // is illegal --> exception
				+ "}";
		final Query q = QueryFactory.create(qStr);

		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);
	}

	@Test(expected = IllegalQueryException.class)
	public void testIllegalPattern2() throws UnsupportedQueryException, IllegalQueryException {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES ?s1 { <http://example1.org> }"
				+ " VALUES ?s2 { <http://example2.org> }"
				+ " VALUES ?s1 { <http://example3.org> }" // assigning the same variable
				+ " SERVICE ?s1 { ?x a ?y }"              // twice is illegal --> exception
				+ " SERVICE ?s2 { ?x a ?y }"
				+ "}";
		final Query q = QueryFactory.create(qStr);

		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);
	}

	@Test(expected = IllegalQueryException.class)
	public void testIllegalPattern3() throws UnsupportedQueryException, IllegalQueryException {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> <http://example2.org>) }"
				+ " VALUES ?s1 { <http://example3.org> }" // assigning the same variable
				+ " SERVICE ?s1 { ?x a ?y }"              // twice is illegal --> exception
				+ " SERVICE ?s2 { ?x a ?y }"
				+ "}";
		final Query q = QueryFactory.create(qStr);

		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);
	}

	@Test
	public void testOneValuesTwoService() throws UnsupportedQueryException, IllegalQueryException {
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
	public void testOneValuesTwoServiceWithSameVariable() throws UnsupportedQueryException, IllegalQueryException {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) { (<http://example1.org> <http://example2.org>) }"
				+ " SERVICE ?s1 { ?x a ?y }"
				+ " SERVICE ?s1 { ?x a ?z }" // same service variable!
				+ "}";
		final Query q = QueryFactory.create(qStr);
		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);

		final String expectedQueryString =
				  "SELECT * WHERE {"
				+ " SERVICE <http://example1.org> { ?x a ?y }"
				+ " SERVICE <http://example1.org> { ?x a ?z }"
				+ " BIND( <http://example1.org> AS ?s1 )"
				+ " BIND( <http://example2.org> AS ?s2 )"   // needs to be added even if not used
				+ "}";
		final Element expectedPattern = QueryFactory.create(expectedQueryString).getQueryPattern();

		assertTrue( q.getQueryPattern().equals(expectedPattern) );
	}

	@Test
	public void testOneValuesClauseWithTwoRowsTwoVariables() throws UnsupportedQueryException, IllegalQueryException {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES (?s1 ?s2) {"
				+ "            (<http://example1.org> <http://example2.org>)"
				+ "            (<http://example3.org> <http://example4.org>)"
				+ "}"
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
	public void testOneValuesClauseWithTwoRowsOneVariable() throws UnsupportedQueryException, IllegalQueryException {
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
	public void testOneValuesClauseWithOneRowWithUndef() throws UnsupportedQueryException, IllegalQueryException {
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
	public void testOneValuesClauseWithTwoRowsWithUndef() throws UnsupportedQueryException, IllegalQueryException {
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

	@Test
	public void testTwoValuesClausesOneScope() throws UnsupportedQueryException, IllegalQueryException {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES ?s1 { <http://example1.org> }"
				+ " VALUES ?s2 { <http://example2.org> <http://example3.org> }"
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
				+ "   SERVICE <http://example1.org> { ?x a ?y }"
				+ "   SERVICE <http://example3.org> { ?z a ?y }"
				+ "   BIND( <http://example1.org> AS ?s1 )"
				+ "   BIND( <http://example3.org> AS ?s2 )"
				+ " }"
				+ "}";
		final Element expectedPattern = QueryFactory.create(expectedQueryString).getQueryPattern();

		final ElementTransform t = new ElementTransformCleanGroupsOfOne();
		final Element expectedPattern2 = ElementTransformer.transform(expectedPattern, t);

		assertTrue( q.getQueryPattern().equals(expectedPattern2) );
	}

	@Test
	public void testTwoValuesClausesTwoScopes() throws UnsupportedQueryException, IllegalQueryException {
		final String qStr =
				  "SELECT * WHERE {"
				+ " VALUES ?s1 { <http://example1.org> }"
				+ " SERVICE ?s1 { ?x a ?y }"
				+ " VALUES ?s2 { <http://example2.org> <http://example3.org> }"
				+ " SERVICE ?s2 { ?z a ?y }"
				+ "}";
		final Query q = QueryFactory.create(qStr);
		ValuesServiceQueryResolver.expandValuesPlusServicePattern(q);

		final String expectedQueryString =
				  "SELECT * WHERE {"
				+ " {"
				+ "   SERVICE <http://example1.org> { ?x a ?y }"
				+ "   BIND( <http://example1.org> AS ?s1 )"
				+ " }"
				+ " {"
				+ "     SERVICE <http://example2.org> { ?z a ?y }"
				+ "     BIND( <http://example2.org> AS ?s2 )"
				+ " } UNION {"
				+ "     SERVICE <http://example3.org> { ?z a ?y }"
				+ "     BIND( <http://example3.org> AS ?s2 )"
				+ " }"
				+ "}";
		final Element expectedPattern = QueryFactory.create(expectedQueryString).getQueryPattern();

		final ElementTransform t = new ElementTransformCleanGroupsOfOne();
		final Element expectedPattern2 = ElementTransformer.transform(expectedPattern, t);

		assertTrue( q.getQueryPattern().equals(expectedPattern2) );
	}

}
