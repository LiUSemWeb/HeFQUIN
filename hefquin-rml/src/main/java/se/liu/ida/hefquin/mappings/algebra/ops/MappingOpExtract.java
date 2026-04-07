package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;
import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceReference;
import se.liu.ida.hefquin.mappings.sources.SourceType;

public class MappingOpExtract< DDS extends DataObject,
                               DC1 extends DataObject,
                               DC2 extends DataObject,
                               QL1 extends Query,
                               QL2 extends Query >
       extends BaseForMappingOperator
{
	protected final SourceReference sr;
	protected final SourceType<DDS, DC1, DC2, QL1, QL2> type;
	protected final QL1 query;
	protected final Map<String, QL2> P;

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
		this.P = P;

		schema = P.keySet();
	}

	@Override
	public int getExpectedNumberOfSubExpressions() { return 0; }

	public SourceReference getSourceReference() { return sr; }

	public SourceType<DDS, DC1, DC2, QL1, QL2> getSourceType() { return type; }

	public QL1 getQuery() { return query; }

	public int getSizeOfP() { return P.size(); }

	public Iterable<Map.Entry<String, QL2>> getEntriesOfP() { return P.entrySet(); }

	@Override
	public Set<String> getSchema() { return schema; }

	@Override
	public boolean isValid() { return true; }

	@Override
	public void visit( final MappingOperatorVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean isValidInput( final Map<SourceReference, DataObject> srMap ) {
		final DataObject d = srMap.get(sr);
		return ( d != null && type.isRelevantDataObject(d) );
	}
}
