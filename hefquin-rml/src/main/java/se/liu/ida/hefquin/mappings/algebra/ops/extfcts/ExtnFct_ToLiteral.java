package se.liu.ida.hefquin.mappings.algebra.ops.extfcts;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;

/**
 * Given a string literal and a datatype IRI, returns a literal whose
 * lexical form is the lexical form of the given string literal and
 * whose datatype IRI is the IRI given as second argument. Additionally,
 * for the sake of robustness, the function also works for the following
 * two types of cases:
 * <p>
 * First, if the given literal is not a string literal but of the same
 * type as the given datatype IRI, then simply return that literal.
 * <p>
 * Second, if the given literal is not a string literal but the given
 * datatype IRI is xsd:string, then simply use the lexical form of the
 * given literal to create an xsd:string literal.
 */
public class ExtnFct_ToLiteral implements ExtensionFunction
{
	public static final ExtensionFunction instance = new ExtnFct_ToLiteral();

	protected ExtnFct_ToLiteral() {}

	@Override
	public boolean isCorrectNumberOfArgument( final int n ) {
		return n == 2;
	}

	@Override
	public Node apply( final Node ... args ) {
		assert args.length == 2;

		if ( args[0].isLiteral() && args[1].isURI() ) {
			final RDFDatatype dtArg1 = args[0].getLiteralDatatype();
			final RDFDatatype dt = NodeFactory.getType( args[1].getURI() );

			if (    dtArg1.equals(XSDDatatype.XSDstring)
			     || dtArg1.equals(dt)                     // first extra case
			     || dt.equals(XSDDatatype.XSDstring) ) {  // second extra case
				final String lex = args[0].getLiteralLexicalForm();

				return NodeFactory.createLiteralDT(lex, dt);
			}
		}

		return MappingRelation.errorNode;
	}

	@Override
	public String toString() {
		return "toLiteral";
	}

}
