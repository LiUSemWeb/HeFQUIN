package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.mappings.algebra.MappingTuple;
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

	@Override
	public Set<String> getSchema() {
		return schema;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean isValidInput( final Map<SourceReference, DataObject> srMap ) {
		final DataObject d = srMap.get(sr);
		return ( d != null && type.isRelevantDataObject(d) );
	}

	@Override
	public Iterator<MappingTuple> evaluate( final Map<SourceReference, DataObject> srMap ) {
		final DataObject d = srMap.get(sr);
		@SuppressWarnings("unchecked")
		final DDS dd = (DDS) d;
		return new MyIterator(dd);
	}

	protected class MyIterator implements Iterator<MappingTuple> {
		protected final DDS d;

		protected Iterator<DC1> itContextObjs = null;
		protected Iterator<MappingTuple> itResultPart = null;

		public MyIterator( final DDS d ) { this.d = d; }

		public boolean hasNext() {
			if ( itContextObjs == null ) {
				itContextObjs = type.eval(query, d).iterator();
			}

			while ( itResultPart == null || ! itResultPart.hasNext() ) {
				if ( ! itContextObjs.hasNext() ) {
					return false;
				}

				final DC1 cxtObj = itContextObjs.next();
				final List<MappingTuple> X_d = determineMappingTuples(d, cxtObj);

				itResultPart = ( X_d == null ) ? null : X_d.iterator();
			}

			return true;
		}

		public MappingTuple next() {
			if ( ! hasNext() ) throw new NoSuchElementException();

			return itResultPart.next();
		}
	}

	protected List<MappingTuple> determineMappingTuples( final DDS d, final DC1 cxtObj ) {
		final List<Node[]> valsPerAttr = determineValuesPerAttribute(d, cxtObj);

		if ( valsPerAttr == null || valsPerAttr.isEmpty() )
			return null;

		final List<MappingTuple> result = new ArrayList<>();
		createMappingTuples( attributesOfP.length - 1, valsPerAttr, new HashMap<>(), result );
		return result;
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

	protected void createMappingTuples( final int i,
	                                    final List<Node[]> valsPerAttr,
	                                    final Map<String, Node> current,
	                                    final List<MappingTuple> result ) {
		final Node[] ithValues = valsPerAttr.get(i);
		for ( int j = 0; j < ithValues.length; j++ ) {
			current.put( attributesOfP[i], ithValues[j] );

			if ( i > 0 ) {
				createMappingTuples(i-1, valsPerAttr, current, result);
			}
			else {
				final Map<String, Node> copy = new HashMap<>(current);
				final MappingTuple t = createMappingTuple(copy);
				result.add(t);
			}
		}
	}

	protected MappingTuple createMappingTuple( final Map<String, Node> map ) {
		return new MappingTuple() {
			@Override
			public Node getValue( final String attr ) { return map.get(attr); }
			@Override
			public Set<String> getSchema() { return schema; }
		};
	}

}
