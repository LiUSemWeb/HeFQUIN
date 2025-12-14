package se.liu.ida.hefquin.mappings.algebra.exprs.fcts;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtensionFunction;

/**
 * Given an arbitrary number string literals, returns a string literal
 * whose lexical form is the concatenation of the given strings.
 */
public class ExtnFct_Concat implements ExtensionFunction
{
	public static final ExtensionFunction instance = new ExtnFct_Concat();

	protected ExtnFct_Concat() {}

	@Override
	public boolean isCorrectNumberOfArgument( final int n ) {
		return n > 0;
	}

	@Override
	public Node apply( final Node ... args ) {
		assert args.length > 0;

		// Special case: If we have a single argument only and this argument
		// is a string literal, we can simply return this argument as result.
		if (    args.length == 1
		     && args[0].isLiteral()
		     && args[0].getLiteralDatatype().equals(XSDDatatype.XSDstring) ) {
			return args[0];
		}

		// General case: Iterate over the arguments and concatenate the strings.
		String result = "";
		for ( int i = 0; i < args.length; i++ ) {
			if (    ! args[i].isLiteral()
			     || ! args[i].getLiteralDatatype().equals(XSDDatatype.XSDstring) )
				return MappingRelation.errorNode;

			result += args[i].getLiteralLexicalForm();
		}

		return NodeFactory.createLiteralString(result);
	}

	@Override
	public String toString() {
		return "concat";
	}

}
