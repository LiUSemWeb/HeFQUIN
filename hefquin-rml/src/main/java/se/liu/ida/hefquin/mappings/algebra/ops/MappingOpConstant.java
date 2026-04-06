package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceReference;

/**
 * This operator simply returns the tuples given to its constructor.
 */
public class MappingOpConstant extends BaseForMappingOperator
{
	protected final MappingRelation r;

	public MappingOpConstant( final MappingRelation r ) {
		assert r != null;
		this.r = r;
	}

	public MappingRelation getMappingRelation() {
		return r;
	}

	@Override
	public int getExpectedNumberOfSubExpressions() { return 0; }

	@Override
	public Set<String> getSchema() {
		return new HashSet<>( r.getSchema() );
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void visit( final MappingOperatorVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean isValidInput( final Map<SourceReference, DataObject> srMap ) {
		return true;
	}

	@Override
	public MappingRelation evaluate( final Map<SourceReference, DataObject> srMap ) {
		return r;
	}

}
