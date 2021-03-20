package se.liu.ida.hefquin.data.jenaimpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.query.TriplePattern;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedTriplePattern;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedTriplePatternUtils;

public class TriplesToSolMapsConverterTest
{
	@Test
	public void noVars() {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( s, p, o, s, p, o );
		verifyOneSolMap( it, new Var[] {}, new Node[] {} );
	}

	@Test
	public void varInS() {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Var v = Var.alloc("v");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( s, p, o, v, p, o );
		verifyOneSolMap( it, new Var[] {v}, new Node[] {s} );
	}

	@Test
	public void varInP() {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Var v = Var.alloc("v");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( s, p, o, s, v, o );
		verifyOneSolMap( it, new Var[] {v}, new Node[] {p} );
	}

	@Test
	public void varInO() {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Var v = Var.alloc("v");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( s, p, o, s, p, v );
		verifyOneSolMap( it, new Var[] {v}, new Node[] {o} );
	}

	@Test
	public void varInSP() {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( s, p, o, v1, v2, o );
		verifyOneSolMap( it, new Var[] {v1,v2}, new Node[] {s,p} );
	}

	@Test
	public void varInSO() {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( s, p, o, v1, p, v2 );
		verifyOneSolMap( it, new Var[] {v1,v2}, new Node[] {s,o} );
	}

	@Test
	public void varInPO() {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( s, p, o, s, v1, v2 );
		verifyOneSolMap( it, new Var[] {v1,v2}, new Node[] {p,o} );
	}

	@Test
	public void varInSPO() {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Var v3 = Var.alloc("v3");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( s, p, o, v1, v2, v3 );
		verifyOneSolMap( it, new Var[] {v1,v2,v3}, new Node[] {s,p,o} );
	}

	@Test
	public void sameVarInSP() {
		final Node sp = NodeFactory.createURI("http://example.org/sp");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Var v = Var.alloc("v");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( sp, sp, o, v, v, o );
		verifyOneSolMap( it, new Var[] {v}, new Node[] {sp} );
	}

	@Test
	public void sameVarInPO() {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node po = NodeFactory.createURI("http://example.org/po");
		final Var v = Var.alloc("v");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( s, po, po, s, v, v );
		verifyOneSolMap( it, new Var[] {v}, new Node[] {po} );
	}

	@Test
	public void sameVarInSO() {
		final Node so = NodeFactory.createURI("http://example.org/so");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Var v = Var.alloc("v");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( so, p, so, v, p, v );
		verifyOneSolMap( it, new Var[] {v}, new Node[] {so} );
	}

	@Test
	public void sameVarInSPO() {
		final Node x = NodeFactory.createURI("http://example.org/x");
		final Var v = Var.alloc("v");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( x, x, x, v, v, v );
		verifyOneSolMap( it, new Var[] {v}, new Node[] {x} );
	}

	@Test
	public void sameVarInSPotherInO() {
		final Node sp = NodeFactory.createURI("http://example.org/sp");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( sp, sp, o, v1, v1, v2 );
		verifyOneSolMap( it, new Var[] {v1,v2}, new Node[] {sp,o} );
	}

	@Test
	public void sameVarInPOotherInS() {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node po = NodeFactory.createURI("http://example.org/po");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( s, po, po, v1, v2, v2 );
		verifyOneSolMap( it, new Var[] {v1,v2}, new Node[] {s,po} );
	}

	@Test
	public void sameVarInSOotherInP() {
		final Node so = NodeFactory.createURI("http://example.org/so");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( so, p, so, v1, v2, v1 );
		verifyOneSolMap( it, new Var[] {v1,v2}, new Node[] {so,p} );
	}

	@Test
	public void fourTriples() {
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Var v = Var.alloc("v");

		final JenaBasedTriple t = JenaBasedTripleUtils.createJenaBasedTriple(s,p,o);
		final List<JenaBasedTriple> l = new ArrayList<>();
		final int n = 4;
		for ( int i = 0; i < n; ++i ) {
			l.add(t);
		}

		final JenaBasedTriplePattern tp = JenaBasedTriplePatternUtils.createJenaBasedTriplePattern( v, p, o );

		final Iterator<? extends SolutionMapping> it = createIteratorForTests( l, tp );

		for ( int i = 0; i < n; ++i ) {
			assertTrue( it.hasNext() );

			final SolutionMapping sm = it.next();
			assertTrue( sm instanceof JenaBasedSolutionMapping );

			final Binding b = ( (JenaBasedSolutionMapping) sm ).asJenaBinding();
			assertEquals( 1, b.size() );

			verifyOneVar( b, v, s );
		}

		assertFalse( it.hasNext() );
	}


	protected void verifyOneSolMap( final Iterator<? extends SolutionMapping> it, final Var[] vars, final Node[] expectedNodes ) {
		assertTrue( it.hasNext() );

		final SolutionMapping sm = it.next();
		assertTrue( sm instanceof JenaBasedSolutionMapping );

		final Binding b = ( (JenaBasedSolutionMapping) sm ).asJenaBinding();
		assertEquals( vars.length, b.size() );

		for( int i = 0; i < vars.length; ++i ) {
			verifyOneVar( b, vars[i], expectedNodes[i] );
		}

		assertFalse( it.hasNext() );
	}

	protected void verifyOneVar( final Binding b, final Var v, final Node expectedNode ) {
		assertTrue( b.contains(v) );
		assertEquals( expectedNode, b.get(v) );
	}

	protected Iterator<? extends SolutionMapping> createIteratorForTests( final Node tS, final Node tP, final Node tO, final Node tpS, final Node tpP, final Node tpO ) {
		final TriplePattern tp = JenaBasedTriplePatternUtils.createJenaBasedTriplePattern( tpS, tpP, tpO );
		return createIteratorForTests( tS, tP, tO, tp );
	}

	protected Iterator<? extends SolutionMapping> createIteratorForTests( final Node tS, final Node tP, final Node tO, final TriplePattern tp ) {
		return createIteratorForTests( JenaBasedTripleUtils.createJenaBasedTriple(tS,tP,tO), tp );
	}

	protected Iterator<? extends SolutionMapping> createIteratorForTests( final JenaBasedTriple t, final TriplePattern tp ) {
		final List<JenaBasedTriple> l = new ArrayList<>();
		l.add(t);
		return createIteratorForTests( l, tp );
	}

	protected Iterator<? extends SolutionMapping> createIteratorForTests( final List<JenaBasedTriple> l, final TriplePattern tp ) {
		return createIteratorForTests( l.iterator(), tp );
	}

	protected Iterator<? extends SolutionMapping> createIteratorForTests( final Iterator<JenaBasedTriple> it, final TriplePattern tp ) {
		return TriplesToSolMapsConverter.convert(it, tp);
	}

}
