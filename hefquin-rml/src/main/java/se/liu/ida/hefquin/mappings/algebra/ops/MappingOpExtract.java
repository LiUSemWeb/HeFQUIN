package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithColumnLayout;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithoutTuples;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceType;

public class MappingOpExtract< DDS extends DataObject,
                               DC1 extends DataObject,
                               DC2 extends DataObject,
                               QL1 extends Query,
                               QL2 extends Query >
       extends BaseForMappingOperator
{
	protected final SourceReference sr;
	protected final SourceType< DDS, DC1, DC2, QL1, QL2> type;
	protected final QL1 query;
	protected final String[] attributesOfP;
	protected final List<QL2> queriesOfP;

	protected final Set<String> schema;

	public MappingOpExtract( final SourceReference sr,
	                         final SourceType<DDS, DC1, DC2, QL1, QL2> type,
	                         final QL1 query,
	                         final Map<String, QL2> P ) {
		assert sr     != null;
		assert type   != null;
		assert query  != null;
		assert P      != null;
		assert ! P.isEmpty();

		this.sr    = sr;
		this.type  = type;
		this.query = query;

		attributesOfP = new String[ P.size() ];
		queriesOfP = new ArrayList<>( P.size() );
		int i = 0;
		for ( final Map.Entry<String, QL2> p : P.entrySet() ) {
			attributesOfP[i++] = p.getKey();
			queriesOfP.add( p.getValue() );
		}

		schema = P.keySet();
	}

	public SourceReference getSourceReference() { return sr; }

	@Override
	public Set<String> getSchema() { return schema; }

	@Override
	public boolean isValid() { return true; }

	@Override
	public boolean isValidInput( final Map<SourceReference, DataObject> srMap ) {
		final DataObject d = srMap.get(sr);
		return ( d != null && type.isRelevantDataObject(d) );
	}

	@Override
	public MappingRelation evaluate( final Map<SourceReference, DataObject> srMap ) {
		final DataObject d = srMap.get(sr);
		@SuppressWarnings("unchecked")
		final DDS dd = (DDS) d;

		return new MyMappingRelation(dd);
	}

	protected class MyMappingRelation implements MappingRelation {
		protected final List<String> schema;
		protected MappingRelationCursor cursor;

		public MyMappingRelation( final DDS d ) {
			schema = Arrays.asList(attributesOfP);
			cursor = new MyCursor(this, d);
		}

		@Override
		public List<String> getSchema() { return schema; }

		@Override
		public MappingRelationCursor getCursor() {
			if ( cursor == null )
				throw new UnsupportedOperationException();

			final MappingRelationCursor c = cursor;
			cursor = null;
			return c;
		}
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
				itContextObjs = type.eval(query, d).iterator();

				if ( ! itContextObjs.hasNext() ) return false;

				final DC1 cxtObj = itContextObjs.next();
				currentCursor = createMappingRelation(d, cxtObj).getCursor();
				nextCursor = currentCursor;
			}

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
		final List<Node[]> valsPerAttr = determineValuesPerAttribute(d, cxtObj);

		if ( valsPerAttr == null ) {
			return new MappingRelationImplWithoutTuples(attributesOfP);
		}

		final Node[][] columns = new MappingRelationCreator(valsPerAttr).getTuples();
		return MappingRelationImplWithColumnLayout.createBasedOnColumns(attributesOfP, columns);
	}

	protected List<Node[]> determineValuesPerAttribute( final DDS d, final DC1 cxtObj ) {
		final List<Node[]> result = new ArrayList<>( attributesOfP.length );
		for ( final QL2 query2 : queriesOfP ) {
			final List<DC2> resultObjs = type.eval(query2, d, cxtObj);

			if ( resultObjs == null || resultObjs.isEmpty() ) {
				result.clear();
				return null;
			}

			final Node[] resultNodes = new Node[ resultObjs.size() ];
			int i = 0;
			for ( final DC2 resultObj : resultObjs ) {
				resultNodes[i++] = type.cast(resultObj);
			}

			result.add(resultNodes);
		}

		return result;
	}

	protected static class MappingRelationCreator {
		protected final List<Node[]> valsPerAttr;
		protected final Node[][] tuples;

		protected int idxOfCurrentRow = 0;

		public MappingRelationCreator( final List<Node[]> valsPerAttr ) {
			this.valsPerAttr = valsPerAttr;

			int length = 1;
			for ( final Node[] ithValues : valsPerAttr )
				length *= ithValues.length;

			this.tuples = new Node[ valsPerAttr.size() ][ length ];

			final Node[] currentTuple = new Node[ valsPerAttr.size() ];
			populateTuples( valsPerAttr.size() - 1, currentTuple );
		}

		protected void populateTuples( final int col,
		                               final Node[] currentTuple ) {
			final Node[] ithValues = valsPerAttr.get(col);
			for ( int j = 0; j < ithValues.length; j++ ) {
				currentTuple[col] = ithValues[j];

				if ( col > 0 ) {
					populateTuples(col-1, currentTuple);
				}
				else {
					for ( int idxOfAttribute = 0; idxOfAttribute < tuples.length; idxOfAttribute++ )
						tuples[idxOfAttribute][idxOfCurrentRow] = currentTuple[idxOfAttribute];

					idxOfCurrentRow++;
				}
			}
		}

		public Node[][] getTuples() { return tuples; }
	}
}
