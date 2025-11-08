package se.liu.ida.hefquin.jenaext.query;

import org.apache.jena.query.Syntax;

public class SyntaxForHeFQUIN extends Syntax
{
	public static final Syntax syntaxSPARQL_12_HeFQUIN = new SyntaxForHeFQUIN("http://ida.liu.se/HeFQUIN/system#SPARQL_12_HeFQUIN");

	public SyntaxForHeFQUIN( final String s ) { super(s); }
}
