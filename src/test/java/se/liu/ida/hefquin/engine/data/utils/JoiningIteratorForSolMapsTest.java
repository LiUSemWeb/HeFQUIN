package se.liu.ida.hefquin.engine.data.utils;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;

public class JoiningIteratorForSolMapsTest extends EngineTestBase
{
	@Test
	public void joinTest() {
		// create solution mappings for first input
		final List<SolutionMapping> i1 = new ArrayList<>();
		// - will have one join partner
		i1.add( createSolMap("x", "http://example1.org", "y", "http://example1.org") );
		// - will have the same join partner as the previous sol.map.
		i1.add( createSolMap("x", "http://example1.org", "y", "http://example2.org") );
		// - will have two join partners
		i1.add( createSolMap("x", "http://example2.org") );
		// - will have no join partners
		i1.add( createSolMap("x", "http://example3.org") );

		// create solution mappings for second input
		final List<SolutionMapping> i2 = new ArrayList<>();
		// - will not be join partner
		i2.add( createSolMap("x", "http://example4.org") );
		// - will be one of the two join partners for third
		i2.add( createSolMap("x", "http://example2.org", "z", "http://example1.org") );
		// - will be join partner for first and second
		i2.add( createSolMap("x", "http://example1.org") );
		// - will be one of the two join partners for third
		i2.add( createSolMap("x", "http://example2.org", "z", "http://example2.org") );

		final Iterator<SolutionMapping> it = new JoiningIteratorForSolMaps(i1, i2);

		final Var x = Var.alloc("x");
		final Var y = Var.alloc("y");
		final Var z = Var.alloc("z");

		assertHasNext( it, "http://example1.org", x, "http://example1.org", y );
		assertHasNext( it, "http://example1.org", x, "http://example2.org", y );
		assertHasNext( it, "http://example2.org", x, "http://example1.org", z );
		assertHasNext( it, "http://example2.org", x, "http://example2.org", z );
		assertFalse( it.hasNext() );

		final Iterator<SolutionMapping> itSwap = new JoiningIteratorForSolMaps(i2, i1);

		assertHasNext( itSwap, "http://example2.org", x, "http://example1.org", z );
		assertHasNext( itSwap, "http://example1.org", x, "http://example1.org", y );
		assertHasNext( itSwap, "http://example1.org", x, "http://example2.org", y );
		assertHasNext( itSwap, "http://example2.org", x, "http://example2.org", z );
		assertFalse( itSwap.hasNext() );
	}

	@Test
	public void joinTestEmpty() {
		// create solution mappings for first input
		final List<SolutionMapping> i1 = new ArrayList<>();
		// - will have no join partner
		i1.add( createSolMap("x", "http://example1.org", "y", "http://example1.org") );

		// second input is empty!
		final List<SolutionMapping> i2 = new ArrayList<>();

		final Iterator<SolutionMapping> it = new JoiningIteratorForSolMaps(i1, i2);
		assertFalse( it.hasNext() );

		final Iterator<SolutionMapping> itSwap = new JoiningIteratorForSolMaps(i2, i1);
		assertFalse( itSwap.hasNext() );
	}


	// ----- helper methods ------------

	/**
	 * Use this method instead of {@link SolutionMappingUtils#createSolutionMapping(Var, Node)}
	 * to make ensure new java objects for the variables and RDF terms.
	 */
	protected SolutionMapping createSolMap( final String varName, final String uriString ) {
		final BindingBuilder b = BindingBuilder.create();
		b.add( Var.alloc(varName), NodeFactory.createURI(uriString) );
		return new SolutionMappingImpl( b.build() );
	}

	/**
	 * Use this method instead of {@link SolutionMappingUtils#createSolutionMapping(Var, Node, Var, Node)}
	 * to make ensure new java objects for the variables and RDF terms.
	 */
	protected SolutionMapping createSolMap( final String varName1, final String uriString1, final String varName2, final String uriString2 ) {
		final BindingBuilder b = BindingBuilder.create();
		b.add( Var.alloc(varName1), NodeFactory.createURI(uriString1) );
		b.add( Var.alloc(varName2), NodeFactory.createURI(uriString2) );
		return new SolutionMappingImpl( b.build() );
	}

}
