package se.liu.ida.hefquin.jenaext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

public class PatternVarsAllTest
{
	@Test
	public void varsInBGP() {
		final String qStr = "SELECT * WHERE { ?s a ?o }";
		final Query q = QueryFactory.create(qStr);
		final Collection<Var> vars = PatternVarsAll.vars( q.getQueryPattern() );

		assertEquals( 2, vars.size() );

		final Var s = Var.alloc("s");
		assertTrue( vars.contains(s) );

		final Var o = Var.alloc("o");
		assertTrue( vars.contains(o) );
	}

	@Test
	public void varsInFilter() {
		final String qStr = "SELECT * WHERE { ?s a ?o FILTER ( ?f = <http://example.org> ) }";
		final Query q = QueryFactory.create(qStr);
		final Collection<Var> vars = PatternVarsAll.vars( q.getQueryPattern() );

		assertEquals( 3, vars.size() );

		final Var s = Var.alloc("s");
		assertTrue( vars.contains(s) );

		final Var o = Var.alloc("o");
		assertTrue( vars.contains(o) );

		final Var f = Var.alloc("f");
		assertTrue( vars.contains(f) );
	}

	@Test
	public void varsInFilterNotExists() {
		final String qStr = "SELECT * WHERE { ?s a ?o FILTER NOT EXISTS { ?f a ?o } }";
		final Query q = QueryFactory.create(qStr);
		final Collection<Var> vars = PatternVarsAll.vars( q.getQueryPattern() );

		assertEquals( 3, vars.size() );

		final Var s = Var.alloc("s");
		assertTrue( vars.contains(s) );

		final Var o = Var.alloc("o");
		assertTrue( vars.contains(o) );

		final Var f = Var.alloc("f");
		assertTrue( vars.contains(f) );
	}

	@Test
	public void varsInFilterExists() {
		final String qStr = "SELECT * WHERE { ?s a ?o FILTER EXISTS { ?f a ?o } }";
		final Query q = QueryFactory.create(qStr);
		final Collection<Var> vars = PatternVarsAll.vars( q.getQueryPattern() );

		assertEquals( 3, vars.size() );

		final Var s = Var.alloc("s");
		assertTrue( vars.contains(s) );

		final Var o = Var.alloc("o");
		assertTrue( vars.contains(o) );

		final Var f = Var.alloc("f");
		assertTrue( vars.contains(f) );
	}

	@Test
	public void varsInMinus() {
		final String qStr = "SELECT * WHERE { ?s a ?o MINUS { ?m a ?o } }";
		final Query q = QueryFactory.create(qStr);
		final Collection<Var> vars = PatternVarsAll.vars( q.getQueryPattern() );

		assertEquals( 3, vars.size() );

		final Var s = Var.alloc("s");
		assertTrue( vars.contains(s) );

		final Var o = Var.alloc("o");
		assertTrue( vars.contains(o) );

		final Var m = Var.alloc("m");
		assertTrue( vars.contains(m) );
	}

}
