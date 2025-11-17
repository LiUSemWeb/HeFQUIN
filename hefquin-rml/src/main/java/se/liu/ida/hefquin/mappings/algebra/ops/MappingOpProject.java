package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingTuple;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

public class MappingOpProject extends BaseForMappingOperator
{
	protected final MappingOperator subOp;
	protected final Set<String> P;

	protected final Set<String> schema;
	protected final boolean valid;

	public MappingOpProject( final MappingOperator subOp, final Set<String> P ) {
		assert subOp != null;
		assert P != null;
		assert ! P.isEmpty();

		this.subOp = subOp;
		this.P = P;

		final Set<String> schemaOfSubOp = subOp.getSchema();
		if ( schemaOfSubOp.containsAll(P) ) {
			schema = P;
			valid = subOp.isValid();
		}
		else {
			valid = false;
			schema = new HashSet<>();
			for ( final String a : P ) {
				if ( schemaOfSubOp.contains(a) )
					schema.add(a);
			}
		}
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
		return subOp.isValidInput(srMap);
	}

	@Override
	public Iterator<MappingTuple> evaluate( final Map<SourceReference, DataObject> srMap ) {
		// TODO Auto-generated method stub
		return null;
	}

	protected class MyIterator implements Iterator<MappingTuple> {
		protected final Iterator<MappingTuple> input;

		public MyIterator( final Iterator<MappingTuple> input ) {
			this.input = input;
		}

		@Override
		public boolean hasNext() {
			return input.hasNext();
		}

		@Override
		public MappingTuple next() {
			final MappingTuple t = input.next();

			return new MappingTuple() {
				@Override
				public Node getValue( final String attribute ) {
					if ( P.contains(attribute) )
						return t.getValue(attribute);
					else
						return null;
				}

				@Override public Set<String> getSchema() { return schema; }
			};
		}
	}

}
