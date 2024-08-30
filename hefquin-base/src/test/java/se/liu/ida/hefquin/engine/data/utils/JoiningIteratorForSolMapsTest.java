package se.liu.ida.hefquin.engine.data.utils;

import static org.junit.Assert.assertFalse;
import static se.liu.ida.hefquin.testutils.AssertExt.assertHasNext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class JoiningIteratorForSolMapsTest extends TestsForSolutionMappingsIterators
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

}
