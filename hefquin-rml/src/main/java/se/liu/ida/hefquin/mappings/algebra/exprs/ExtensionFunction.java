package se.liu.ida.hefquin.mappings.algebra.exprs;

import org.apache.jena.graph.Node;

public interface ExtensionFunction
{
	boolean isCorrectNumberOfArgument( int n );

	Node apply( Node ... args );
}
