package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.Set;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;

public class MappingOpProject implements MappingOperator
{
	protected final Set<String> P;

	public static MappingOpProject createWithSPOG() {
		return new MappingOpProject( MappingRelation.spogAttrs );
	}

	public MappingOpProject( final Set<String> P ) {
		assert P != null;
		assert ! P.isEmpty();

		this.P = P;
	}

	public Set<String> getP() { return P; }

	@Override
	public int getExpectedNumberOfSubExpressions() { return 1; }

	@Override
	public void visit( final MappingOperatorVisitor visitor ) {
		visitor.visit(this);
	}
}
