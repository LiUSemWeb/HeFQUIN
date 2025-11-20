package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

public class MappingOpUnion extends BaseForMappingOperator
{
	protected final List<MappingOperator> elements;

	protected final Set<String> schema;
	protected final boolean valid;

	public MappingOpUnion( final MappingOperator ... elements ) {
		this( Arrays.asList(elements) );
	}

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
	public MappingRelation evaluate( final Map<SourceReference, DataObject> srMap ) {
		return new MyMappingRelation(srMap);
	}

	protected class MyMappingRelation implements MappingRelation {
		protected final Map<SourceReference, DataObject> srMap;
		protected final List<String> schemaL;

		public MyMappingRelation( final Map<SourceReference, DataObject> srMap ) {
			this.srMap = srMap;
			this.schemaL = new ArrayList<>(schema);
		}

		@Override
		public List<String> getSchema() { return schemaL; }

		@Override
		public MappingRelationCursor getCursor() {
			return new MyCursor(this, srMap);
		}
	}

	protected class MyCursor implements MappingRelationCursor {
		protected final MappingRelation myRelation;
		protected final Map<SourceReference, DataObject> srMap;

		protected final Iterator<MappingOperator> subOpIt;
		protected MappingRelationCursor currentInput = null;

		public MyCursor( final MappingRelation myRelation,
		                 final Map<SourceReference, DataObject> srMap ) {
			this.myRelation = myRelation;
			this.srMap = srMap;
			subOpIt = elements.iterator();
		}

		@Override
		public MappingRelation getMappingRelation() { return myRelation; }

		@Override
		public boolean hasNext() {
			while ( currentInput == null || ! currentInput.hasNext() ) {
				if ( ! subOpIt.hasNext() ) {
					return false;
				}

				currentInput = subOpIt.next().evaluate(srMap).getCursor();
			}

			return true;
		}

		@Override
		public void advance() {
			if ( ! hasNext() )
				throw new UnsupportedOperationException();

			currentInput.advance();
		}

		@Override
		public Node getValueOfCurrentTuple( final int idxOfAttribute ) {
			return currentInput.getValueOfCurrentTuple(idxOfAttribute);
		}
	}

}
