package se.liu.ida.hefquin.query;

import java.util.Set;

public interface BGP extends SPARQLGraphPattern
{
	Set<TriplePattern> getTriplePatterns(); 
}
