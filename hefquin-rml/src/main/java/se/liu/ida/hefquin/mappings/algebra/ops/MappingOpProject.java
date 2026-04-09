package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceReference;

public class MappingOpProject extends BaseForUnaryMappingOperator
{
	protected final Set<String> P;

	protected final Set<String> schema;
	protected final boolean valid;

	public static MappingOpProject createWithSPOG( final MappingOperator subOp ) {
		return new MappingOpProject( subOp, MappingRelation.spogAttrs );
	}

	public MappingOpProject( final MappingOperator subOp, final Set<String> P ) {
		super(subOp);
		assert P != null;
		assert ! P.isEmpty();

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

	public Set<String> getP() { return P; }

	@Override
	public Set<String> getSchema() {
		return schema;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public void visit( final MappingOperatorVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean isValidInput( final Map<SourceReference, DataObject> srMap ) {
		return subOp.isValidInput(srMap);
	}
}
