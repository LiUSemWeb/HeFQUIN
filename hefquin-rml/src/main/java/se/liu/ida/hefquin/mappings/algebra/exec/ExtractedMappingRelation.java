package se.liu.ida.hefquin.mappings.algebra.exec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.impl.BaseForMappingRelationImpl;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithColumnLayout;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithoutTuples;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtract;
import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceType;

/**
 * A {@link MappingRelation} produced by a {@link MappingOpExtract} operator.
 */
public class ExtractedMappingRelation< DDS extends DataObject,
                                       DC1 extends DataObject,
                                       DC2 extends DataObject,
                                       QL1 extends Query,
                                       QL2 extends Query >
		extends BaseForMappingRelationImpl
{
	protected final SourceType<DDS, DC1, DC2, QL1, QL2> srcType;
	protected final QL1 query;
	protected final Iterable<Map.Entry<String, QL2>> entriesOfP;

	protected MappingRelationCursor cursor;

	public ExtractedMappingRelation(
			final List<String> schema,
			final SourceType<DDS, DC1, DC2, QL1, QL2> srcType,
			final QL1 query,
			final Iterable<Map.Entry<String, QL2>> entriesOfP,
			final DDS d ) {
		super(schema);

		this.srcType    = srcType;
		this.query      = query;
		this.entriesOfP = entriesOfP;

		this.cursor = new MyCursor(this, d);
	}

	@Override
	public MappingRelationCursor getCursor() {
		if ( cursor == null )
			throw new UnsupportedOperationException();

		final MappingRelationCursor c = cursor;
		cursor = null;
		return c;
	}

	protected class MyCursor implements MappingRelationCursor {
		protected final MappingRelation r;
		protected final DDS d;

		protected Iterator<DC1> itContextObjs = null;
		protected MappingRelationCursor currentCursor = null;
		protected MappingRelationCursor nextCursor = null;

		public MyCursor( final MappingRelation r, final DDS d ) {
			this.r = r;
			this.d = d;
		}

		@Override
		public MappingRelation getMappingRelation() { return r; }

		@Override
		public Node getValueOfCurrentTuple( final int idxOfAttribute ) {
			if ( currentCursor == null )
				throw new NoSuchElementException();

			return currentCursor.getValueOfCurrentTuple(idxOfAttribute);
		}

		@Override
		public boolean hasNext() {
			if ( itContextObjs == null ) {
				itContextObjs = srcType.eval(query, d).iterator();

				if ( ! itContextObjs.hasNext() ) return false;

				final DC1 cxtObj = itContextObjs.next();
				currentCursor = createMappingRelation(d, cxtObj).getCursor();
				nextCursor = currentCursor;
			}

			// currentCursor may be null in cases in which hasNext() is
			// called a second time where the first call returned false
			// in the previous if-block (i.e., itContextObjs is an empty
			// iterator)
			if ( currentCursor == null ) return false;

			while ( ! currentCursor.hasNext() && ! nextCursor.hasNext() ) {
				if ( ! itContextObjs.hasNext() ) return false;

				final DC1 cxtObj = itContextObjs.next();
				nextCursor = createMappingRelation(d, cxtObj).getCursor();
			}

			return true;
		}

		@Override
		public void advance() {
			if ( ! hasNext() )
				throw new UnsupportedOperationException();

			if ( ! currentCursor.hasNext() )
				currentCursor = nextCursor;

			currentCursor.advance();
		}
	}

	protected MappingRelation createMappingRelation( final DDS d, final DC1 cxtObj ) {
		final Map<String, Node[]> valsPerAttr = determineValuesPerAttribute(d, cxtObj);

		if ( valsPerAttr == null ) {
			return new MappingRelationImplWithoutTuples(schema);
		}

		return new MappingRelationCreator(schema, valsPerAttr).getCreatedRelation();
	}

	/**
	 * The returned map associates an array with every attribute in
	 * {@link MappingOpExtract#getEntriesOfP()}. For every attribute,
	 * this array contains the values that have been determined by
	 * evaluating the corresponding query of the attribute in the
	 * context of the given context object (where the corresponding
	 * query of an attribute is the query in the same entry of
	 * {@link MappingOpExtract#getEntriesOfP()}).
	 *
	 * If there is an attribute for which the corresponding query
	 * has the empty result (i.e., no values), then this function
	 * returns null.
	 */
	protected Map<String, Node[]> determineValuesPerAttribute( final DDS d, final DC1 cxtObj ) {
		final Map<String, Node[]> result = new HashMap<>();
		for ( final Map.Entry<String, QL2> e : entriesOfP ) {
			final String attr = e.getKey();
			final QL2 query = e.getValue();

			final List<DC2> resultObjs = srcType.eval(query, d, cxtObj);

			if ( resultObjs == null || resultObjs.isEmpty() ) {
				result.clear();
				return null;
			}

			final Node[] resultNodes = new Node[ resultObjs.size() ];
			int i = 0;
			for ( final DC2 resultObj : resultObjs ) {
				resultNodes[i++] = srcType.cast(resultObj);
			}

			result.put(attr, resultNodes);
		}

		return result;
	}

	protected static class MappingRelationCreator {
		protected final List<String> schema;
		protected final Map<String, Node[]> valsPerAttr;

		// first dimension is the number of attributes in the schema;
		// second dimension is the number of row in the result, which
		// is the product of the lengths of the arrays in valsPerAttr
		// as we will produce the cross product of the values in these
		// arrays
		protected final Node[][] tuples;

		protected int idxOfCurrentRow = 0;

		public MappingRelationCreator( final List<String> schema,
		                               final Map<String, Node[]> valsPerAttr ) {
			this.schema = schema;
			this.valsPerAttr = valsPerAttr;

			int length = 1;
			for ( final Node[] ithValues : valsPerAttr.values() )
				length *= ithValues.length;

			this.tuples = new Node[ schema.size() ][ length ];

			final Node[] currentTuple = new Node[ schema.size() ];
			populateTuples( schema.size() - 1, currentTuple );
		}

		protected void populateTuples( final int col,
		                               final Node[] currentTuple ) {
			final String attr = schema.get(col);
			final Node[] ithValues = valsPerAttr.get(attr);
			for ( int j = 0; j < ithValues.length; j++ ) {
				currentTuple[col] = ithValues[j];

				if ( col > 0 ) {
					populateTuples(col-1, currentTuple);
				}
				else {
					for ( int i = 0; i < tuples.length; i++ )
						tuples[i][idxOfCurrentRow] = currentTuple[i];

					idxOfCurrentRow++;
				}
			}
		}

		public MappingRelation getCreatedRelation() {
			return MappingRelationImplWithColumnLayout.createBasedOnColumns(
					schema, tuples);
		}
	}

}
