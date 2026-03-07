package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;

public abstract class BaseForNaryMappingOperator extends BaseForMappingOperator
{
	protected final List<MappingOperator> subOps;

	protected BaseForNaryMappingOperator( final MappingOperator ... subOps ) {
		this( Arrays.asList(subOps) );
	}

	protected BaseForNaryMappingOperator( final List<MappingOperator> subOps ) {
		assert subOps != null;
		assert subOps.size() > 1;

		this.subOps = subOps;
	}

	public int getNumberOfSubOps() { return subOps.size(); }

	public Iterable<MappingOperator> getSubOps() { return subOps; }
}
