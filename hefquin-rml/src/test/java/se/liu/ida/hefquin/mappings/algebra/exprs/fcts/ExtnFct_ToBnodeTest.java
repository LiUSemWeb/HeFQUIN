package se.liu.ida.hefquin.mappings.algebra.exprs.fcts;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

public class ExtnFct_ToBnodeTest
{
	@Test
	public void twoDifferentBNodes() {
		final Node s1 = NodeFactory.createLiteralString("a");
		final Node s2 = NodeFactory.createLiteralString("b");

		final Node result1 = new ExtnFct_ToBNode().apply(s1);
		final Node result2 = new ExtnFct_ToBNode().apply(s2);

		assertTrue( result1.isBlank() );
		assertTrue( result2.isBlank() );
		assertFalse( result1.equals(result2) ); // different bnode expected
	}

	@Test
	public void sameBNodeTwice() {
		final Node s1 = NodeFactory.createLiteralString("a");
		final Node s2 = NodeFactory.createLiteralString("a"); // same!

		final Node result1 = new ExtnFct_ToBNode().apply(s1);
		final Node result2 = new ExtnFct_ToBNode().apply(s2);

		assertTrue( result1.isBlank() );
		assertTrue( result2.isBlank() );
		assertTrue( result1.equals(result2) ); // same bnode expected!
	}

}
