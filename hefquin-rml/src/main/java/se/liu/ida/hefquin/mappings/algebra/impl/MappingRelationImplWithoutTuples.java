package se.liu.ida.hefquin.mappings.algebra.impl;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;

public class MappingRelationImplWithoutTuples extends BaseForMappingRelationImpl
{
	public MappingRelationImplWithoutTuples( final String ... schema ) {
		super(schema);
	}

	public MappingRelationImplWithoutTuples( final List<String> schema ) {
		super(schema);
	}

	@Override
	public MappingRelationCursor getCursor() { return new MyCursor(this); }


	protected static class MyCursor implements MappingRelationCursor {
		protected final MappingRelation r;

		public MyCursor( final MappingRelation r ) { this.r = r; }

		@Override
		public MappingRelation getMappingRelation() { return r; }

		@Override
		public boolean hasNext() { return false; }

		@Override
		public void advance() { throw new UnsupportedOperationException(); }

		@Override
		public Node getValueOfCurrentTuple( final int idxOfAttribute ) {
			throw new NoSuchElementException();
		}
	}

}
