package se.liu.ida.hefquin.mappings.algebra.ops;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;

/**
 * This operator simply returns the tuples given to its constructor.
 */
public class MappingOpConstant implements MappingOperator
{
	protected final MappingRelation r;

	public MappingOpConstant( final MappingRelation r ) {
		assert r != null;
		this.r = r;
	}

	public MappingRelation getMappingRelation() {
		return r;
	}

	@Override
	public int getExpectedNumberOfSubExpressions() { return 0; }

	@Override
	public void visit( final MappingOperatorVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public int hashCode() {
		return r.hashCode();
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return o instanceof MappingOpConstant c  &&  c.r.equals(r);
	}

	@Override
	public String toString() {
		return "constant";
	}
}
