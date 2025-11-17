package se.liu.ida.hefquin.mappings.algebra;

import java.util.Set;

import org.apache.jena.graph.Node;

public interface MappingTuple
{
	Set<String> getSchema();

	Node getValue( String attribute );
}
