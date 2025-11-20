package se.liu.ida.hefquin.mappings.algebra;

import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

public interface MappingOperator
{
	/**
	 * Returns an identifier of this operator, which should be distinct from
	 * the identifiers of all other operators within the same plan (no matter
	 * what type of operator they are).
	 */
	int getID();

	Set<String> getSchema();

	boolean isValid();

	boolean isValidInput( Map<SourceReference,DataObject> srMap );

	MappingRelation evaluate( Map<SourceReference,DataObject> srMap );
}
