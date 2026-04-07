package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;
import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceReference;

public class MappingOpUnion extends BaseForNaryMappingOperator
{
	protected final Set<String> schema;
	protected final boolean valid;

	public MappingOpUnion( final MappingOperator ... subOps ) {
		this( Arrays.asList(subOps) );
	}

	public MappingOpUnion( final List<MappingOperator> subOps ) {
		super(subOps);

		schema = new HashSet<>();
		boolean _valid = true;
		for ( final MappingOperator subOp : subOps ) {
			final Set<String> schemaOfSubOp = subOp.getSchema();
			schema.addAll(schemaOfSubOp);

			if ( _valid && ! subOp.isValid() )
				_valid = false;

			if ( _valid && ! schema.equals(schemaOfSubOp) )
				_valid = false;
		}

		valid = _valid;
	}

	@Override
	public Set<String> getSchema() {
		return schema;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public void visit( final MappingOperatorVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean isValidInput( final Map<SourceReference, DataObject> srMap ) {
		for ( final MappingOperator subOp : subOps ) {
			if ( ! subOp.isValidInput(srMap) )
				return false;
		}

		return true;
	}
}
