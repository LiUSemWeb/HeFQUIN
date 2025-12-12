package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.lib.Pair;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

public class MappingOpJoin extends BaseForBinaryMappingOperator
{
	protected final List<Pair<String,String>> J;

	protected final Set<String> schema;
	protected final boolean valid;

	@SafeVarargs
	public MappingOpJoin( final MappingOperator subOp1,
	                      final MappingOperator subOp2,
	                      final Pair<String,String> ... J ) {
		this( subOp1, subOp2, Arrays.asList(J) );
	}

	public MappingOpJoin( final MappingOperator subOp1,
	                      final MappingOperator subOp2,
	                      final List<Pair<String,String>> J ) {
		super(subOp1, subOp2);

		assert J != null;
		this.J = J;

		final Set<String> schemaOfSubOp1 = subOp1.getSchema();
		final Set<String> schemaOfSubOp2 = subOp2.getSchema();

		schema = new HashSet<>();
		schema.addAll(schemaOfSubOp1);
		schema.addAll(schemaOfSubOp2);

		if ( schema.size() != schemaOfSubOp1.size()+schemaOfSubOp2.size() ) {
			valid = false;
		}
		else if ( ! subOp1.isValid() ) {
			valid = false;
		}
		else if ( ! subOp2.isValid() ) {
			valid = false;
		}
		else {
			valid = isValid(J, schemaOfSubOp1, schemaOfSubOp2);
		}
	}

	protected static boolean isValid( final List<Pair<String,String>> J,
	                                  final Set<String> schemaOfSubOp1,
	                                  final Set<String> schemaOfSubOp2 ) {
		for ( final Pair<String,String> j : J ) {
			if ( ! schemaOfSubOp1.contains(j.getLeft()) ) return false;
			if ( ! schemaOfSubOp2.contains(j.getRight()) ) return false;
		}

		return true;
	}

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
		return subOp1.isValidInput(srMap) && subOp2.isValidInput(srMap);
	}

	@Override
	public MappingRelation evaluate( final Map<SourceReference, DataObject> srMap ) {
		// TODO
		throw new UnsupportedOperationException();
	}
}
