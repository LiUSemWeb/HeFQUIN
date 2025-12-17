package se.liu.ida.hefquin.mappings.algebra.impl;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;

public class MappingRelationImplWithColumnLayout extends BaseForMappingRelationImpl
{
	public static MappingRelation createBasedOnColumns( final String[] schema,
	                                                    final Node[] ... columns ) {
		assert schema.length == columns.length;
		assert checkArrayLengths(columns);

		return new MappingRelationImplWithColumnLayout(schema, columns);
	}

	public static MappingRelation createBasedOnColumns( final List<String> schema,
	                                                    final Node[] ... columns ) {
		assert schema.size() == columns.length;
		assert checkArrayLengths(columns);

		return new MappingRelationImplWithColumnLayout(schema, columns);
	}

	public static MappingRelation createBasedOnTuples( final String[] schema,
	                                                   final Node[] ... tuples ) {
		return createBasedOnTuples( Arrays.asList(schema), tuples );
	}

	public static MappingRelation createBasedOnTuples( final List<String> schema,
	                                                   final Node[] ... tuples ) {
		if ( tuples.length == 0 )
			return new MappingRelationImplWithoutTuples(schema);

		final Node[][] columns = new Node[ schema.size() ][ tuples.length ];
		for ( int i = 0; i < tuples.length; i++ ) {
			final Node[] currentTuple = tuples[i];

			assert currentTuple.length == schema.size();

			for ( int attrIdx = 0; attrIdx < schema.size(); attrIdx++ ) {
				columns[attrIdx][i] = currentTuple[attrIdx];
			}
		}

		return new MappingRelationImplWithColumnLayout(schema, columns);
	}

	protected static boolean checkArrayLengths( final Node[][] columns ) {
		if ( columns.length == 0 ) return true;

		for ( int i = 1; i < columns.length; i++ ) {
			if ( columns[0].length != columns[i].length ) return false;
		}

		return true;
	}


	protected final Node[][] columns;

	protected MappingRelationImplWithColumnLayout( final String[] schema,
	                                               final Node[] ... columns ) {
		this( Arrays.asList(schema), columns );
	}

	protected MappingRelationImplWithColumnLayout( final List<String> schema,
	                                               final Node[] ... columns ) {
		super(schema);

		this.columns = columns;
	}

	public Node[][] getColumns() {
		return columns;
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

			return columns[idxOfAttribute][idxOfCurrentTuple];
		}

		@Override
		public boolean hasNext() {
			return ( idxOfCurrentTuple + 1 < columns[0].length );
		}

		@Override
		public void advance() {
			if ( ! hasNext() )
				throw new UnsupportedOperationException();

			idxOfCurrentTuple++;
		}
	}

}
