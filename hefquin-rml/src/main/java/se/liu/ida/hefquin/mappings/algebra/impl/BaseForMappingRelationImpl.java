package se.liu.ida.hefquin.mappings.algebra.impl;

import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;

public abstract class BaseForMappingRelationImpl implements MappingRelation
{
	protected final List<String> schema;

	protected BaseForMappingRelationImpl( final String[] schema ) {
		this( Arrays.asList(schema) );
	}

	protected BaseForMappingRelationImpl( final List<String> schema ) {
		assert schema != null;
		this.schema = schema;
	}

	@Override
	public List<String> getSchema() { return schema; }

}
