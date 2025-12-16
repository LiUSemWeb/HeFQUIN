package se.liu.ida.hefquin.mappings.algebra.exprs.fcts;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtensionFunction;

/**
 * Given a string literal, returns a blank node that is unique per every
 * possible string literal. That is, calling this function twice with the
 * same string literal will result in the same blank node, whereas calling
 * it twice with two different string literals will result in two different
 * blank nodes.
 */
public class ExtnFct_ToBNode implements ExtensionFunction
{
	public static final ExtensionFunction instance = new ExtnFct_ToBNode();

	protected ExtnFct_ToBNode() {}

	@Override
	public boolean isCorrectNumberOfArgument( final int n ) {
		return n == 1;
	}

	@Override
	public Node apply( final Node ... args ) {
		assert args.length == 1;

		if (    args[0].isLiteral()
		     && args[0].getLiteralDatatype().equals(XSDDatatype.XSDstring) ) {
			final String lex = args[0].getLiteralLexicalForm();
			return NodeFactory.createBlankNode(lex);
		}

		return MappingRelation.errorNode;
	}

	@Override
	public String toString() {
		return "toBNode";
	}

}
