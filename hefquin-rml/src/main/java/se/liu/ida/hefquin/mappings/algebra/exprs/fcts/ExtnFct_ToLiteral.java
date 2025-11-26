package se.liu.ida.hefquin.mappings.algebra.exprs.fcts;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtensionFunction;

/**
 * Given a string literal and a datatype IRI, returns a literal whose
 * lexical form is the lexical form of the given string literal and
 * whose datatype IRI is the IRI given as second argument.
 */
public class ExtnFct_ToLiteral implements ExtensionFunction
{
	@Override
	public boolean isCorrectNumberOfArgument( final int n ) {
		return n == 2;
	}

	@Override
	public Node apply( final Node ... args ) {
		assert args.length == 2;

		if (    args[0].isLiteral()
		     && args[0].getLiteralDatatype().equals(XSDDatatype.XSDstring)
		     && args[1].isURI() ) {
			final String lex = args[0].getLiteralLexicalForm();
			final RDFDatatype dt = NodeFactory.getType( args[1].getURI() );

			return NodeFactory.createLiteralDT(lex, dt);
		}

		return MappingRelation.errorNode;
	}

}
