package se.liu.ida.hefquin.engine.query;

public interface SPARQLServicePattern extends SPARQLGraphPattern
{
	// TODO: capture variable or IRI

	SPARQLGraphPattern getSubPattern();
}
