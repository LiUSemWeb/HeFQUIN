package se.liu.ida.hefquin.mappings.algebra.exprs.fcts;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtensionFunction;

public class ExtnFct_ToIRI implements ExtensionFunction
{
	public static final ExtensionFunction instance = new ExtnFct_ToIRI();

	protected ExtnFct_ToIRI() {}

	@Override
	public boolean isCorrectNumberOfArgument( final int n ) {
		return n == 2;
	}

	@Override
	public Node apply( final Node ... args ) {
		assert args.length == 2;

		if (    args[0].isLiteral()
		     && args[0].getLiteralDatatype().equals(XSDDatatype.XSDstring) ) {
			final String lex = args[0].getLiteralLexicalForm();

			try {
				final URI uri = new URI(lex);
				if ( uri.isAbsolute() )
					return NodeFactory.createURI(lex);
			}
			catch ( final URISyntaxException e ) {
				// just continue after this catch block
			}

			if ( ! args[1].isURI() )
				return MappingRelation.errorNode;

			final String basePlusLex = args[1].getURI() + lex;
			try {
				final URI uri = new URI(basePlusLex);
				if ( uri.isAbsolute() )
					return NodeFactory.createURI(basePlusLex);
			}
			catch ( final URISyntaxException e ) {
				// just continue after this catch block
			}
		}

		return MappingRelation.errorNode;
	}

	@Override
	public String toString() {
		return "toIRI";
	}

}
