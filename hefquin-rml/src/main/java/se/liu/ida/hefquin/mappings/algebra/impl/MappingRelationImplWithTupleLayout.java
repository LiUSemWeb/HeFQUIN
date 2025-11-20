package se.liu.ida.hefquin.mappings.algebra.impl;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;

public class MappingRelationImplWithTupleLayout extends BaseForMappingRelationImpl
{
	protected final Node[][] tuples;

	public MappingRelationImplWithTupleLayout( final String[] schema, final Node[] ... tuples ) {
		this( Arrays.asList(schema), tuples );
	}

	public MappingRelationImplWithTupleLayout( final List<String> schema, final Node[] ... tuples ) {
		super(schema);

		this.tuples = tuples;
	}

	@Override
	public MappingRelationCursor getCursor() {
		return new MyCursor(this);
	}

	protected class MyCursor implements MappingRelationCursor {
		protected final MappingRelation r;
		protected int idxOfCurrentTuple = -1;

		public MyCursor( final MappingRelation r ) { this.r = r; }

		@Override
		public MappingRelation getMappingRelation() {
			return r;
		}

		@Override
		public Node getValueOfCurrentTuple( final int idxOfAttribute ) {
			if ( idxOfCurrentTuple < 0 )
				throw new NoSuchElementException();

			return tuples[idxOfCurrentTuple][idxOfAttribute];
		}

		@Override
		public boolean hasNext() {
			return ( idxOfCurrentTuple + 1 < tuples.length );
		}

		@Override
		public void advance() {
			if ( ! hasNext() )
				throw new UnsupportedOperationException();

			idxOfCurrentTuple++;
		}
	}

}
