package se.liu.ida.hefquin.base.query;

public interface SPARQLServicePattern extends SPARQLGraphPattern
{
	// TODO: capture variable or IRI

	SPARQLGraphPattern getSubPattern();
}
