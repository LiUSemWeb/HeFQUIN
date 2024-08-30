package se.liu.ida.hefquin.testutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public abstract class AssertExt
{
	public static void assertHasNext( final Iterator<SolutionMapping> it,
	                                  final String expectedURIforV1, final Var v1,
	                                  final String expectedURIforV2, final Var v2 )
	{
		assertTrue( it.hasNext() );

		final Binding b = it.next().asJenaBinding();
		assertEquals( 2, b.size() );

		assertEquals( expectedURIforV1, b.get(v1).getURI() );
		assertEquals( expectedURIforV2, b.get(v2).getURI() );
	}

	public static void assertHasNext( final Iterator<SolutionMapping> it,
	                                  final String expectedURIforV1, final Var v1,
	                                  final String expectedURIforV2, final Var v2,
	                                  final String expectedURIforV3, final Var v3 )
	{
		assertTrue( it.hasNext() );

		final Binding b = it.next().asJenaBinding();
		assertEquals( 3, b.size() );

		assertEquals( expectedURIforV1, b.get(v1).getURI() );
		assertEquals( expectedURIforV2, b.get(v2).getURI() );
		assertEquals( expectedURIforV3, b.get(v3).getURI() );
	}
}
