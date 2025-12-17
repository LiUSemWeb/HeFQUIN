package se.liu.ida.hefquin.jenaext.query;

import org.apache.jena.query.Syntax;

import se.liu.ida.hefquin.jenaintegration.HeFQUINConstants;

public class SyntaxForHeFQUIN extends Syntax
{
	public static final Syntax syntaxSPARQL_12_HeFQUIN = new SyntaxForHeFQUIN(HeFQUINConstants.systemVarNS + "SPARQL_12_HeFQUIN");

	public SyntaxForHeFQUIN( final String s ) { super(s); }
}
