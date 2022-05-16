package se.liu.ida.hefquin.engine.data.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class UnionIteratorForSolMapsTest extends TestsForSolutionMappingsIterators
{
	@Test
	public void union() {
		final SolutionMapping s1 = createSolMap("x", "http://example1.org");
		final SolutionMapping s2 = createSolMap("x", "http://example2.org");
		final SolutionMapping s3 = createSolMap("x", "http://example3.org");

		final List<SolutionMapping> i1 = new ArrayList<>();
		i1.add( s1 );
		i1.add( s2 );

		final List<SolutionMapping> i2 = new ArrayList<>();
		i2.add( s3 );

		final Iterator<SolutionMapping> it = new UnionIteratorForSolMaps(i1, i2);

		assertTrue( it.hasNext() );
		assertEquals( s1, it.next() );

		assertTrue( it.hasNext() );
		assertEquals( s2, it.next() );

		assertTrue( it.hasNext() );
		assertEquals( s3, it.next() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void firstEmpty() {
		final SolutionMapping s1 = createSolMap("x", "http://example1.org");

		final List<SolutionMapping> i1 = new ArrayList<>();

		final List<SolutionMapping> i2 = new ArrayList<>();
		i2.add( s1 );

		final Iterator<SolutionMapping> it = new UnionIteratorForSolMaps(i1, i2);

		assertTrue( it.hasNext() );
		assertEquals( s1, it.next() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void secondEmpty() {
		final SolutionMapping s1 = createSolMap("x", "http://example1.org");

		final List<SolutionMapping> i1 = new ArrayList<>();
		i1.add( s1 );

		final List<SolutionMapping> i2 = new ArrayList<>();

		final Iterator<SolutionMapping> it = new UnionIteratorForSolMaps(i1, i2);

		assertTrue( it.hasNext() );
		assertEquals( s1, it.next() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void bothEmpty() {
		final List<SolutionMapping> i1 = new ArrayList<>();
		final List<SolutionMapping> i2 = new ArrayList<>();

		final Iterator<SolutionMapping> it = new UnionIteratorForSolMaps(i1, i2);

		assertFalse( it.hasNext() );
	}

}
