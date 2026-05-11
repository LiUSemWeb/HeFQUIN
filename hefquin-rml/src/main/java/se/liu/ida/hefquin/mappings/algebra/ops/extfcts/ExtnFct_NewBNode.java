package se.liu.ida.hefquin.mappings.algebra.ops.extfcts;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/**
 * A null-ary function that returns a new blank node every time it is called.
 */
public class ExtnFct_NewBNode implements ExtensionFunction
{
	public static final ExtensionFunction instance = new ExtnFct_NewBNode();

	protected ExtnFct_NewBNode() {}

	@Override
	public boolean isCorrectNumberOfArgument( final int n ) {
		return n == 0;
	}

	@Override
	public Node apply( final Node ... args ) {
		assert args.length == 0;

		return NodeFactory.createBlankNode();
	}

	@Override
	public String toString() {
		return "newBNode";
	}
}
