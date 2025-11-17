package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingTuple;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

public class MappingOpUnion extends BaseForMappingOperator
{
	protected final List<MappingOperator> elements;

	protected final Set<String> schema;
	protected final boolean valid;

	public MappingOpUnion( final List<MappingOperator> elements ) {
		assert elements != null;
		assert elements.size() > 1;

		this.elements = elements;

		schema = new HashSet<>();
		boolean _valid = true;
		for ( final MappingOperator subOp : elements ) {
			final Set<String> schemaOfSubOp = subOp.getSchema();
			schema.addAll(schemaOfSubOp);

			if ( _valid && ! subOp.isValid() )
				_valid = false;

			if ( _valid && ! schema.equals(schemaOfSubOp) )
				_valid = false;
		}

		valid = _valid;
	}

	public int size() { return elements.size(); }

	public Iterable<MappingOperator> getElements() { return elements; }

	@Override
	public Set<String> getSchema() {
		return schema;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public boolean isValidInput( final Map<SourceReference, DataObject> srMap ) {
		for ( final MappingOperator subOp : elements ) {
			if ( ! subOp.isValidInput(srMap) )
				return false;
		}

		return true;
	}

	@Override
	public Iterator<MappingTuple> evaluate( final Map<SourceReference, DataObject> srMap ) {
		return new MyIterator(srMap);
	}

	protected class MyIterator implements Iterator<MappingTuple> {
		protected final Map<SourceReference, DataObject> srMap;
		protected final Iterator<MappingOperator> subOpIt;

		protected Iterator<MappingTuple> itResultPart = null;

		public MyIterator( final Map<SourceReference, DataObject> srMap ) {
			this.srMap = srMap;
			subOpIt = elements.iterator();
		}

		@Override
		public boolean hasNext() {
			while ( itResultPart == null || ! itResultPart.hasNext() ) {
				if ( ! subOpIt.hasNext() ) {
					return false;
				}

				itResultPart = subOpIt.next().evaluate(srMap);
			}

			return true;
		}

		@Override
		public MappingTuple next() {
			if ( ! hasNext() )
				throw new NoSuchElementException();

			return itResultPart.next();
		}
	}

}
