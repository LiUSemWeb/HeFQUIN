package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.lib.Pair;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;

public class MappingOpJoin implements MappingOperator
{
	protected final List<Pair<String,String>> J;

	@SafeVarargs
	public MappingOpJoin( final Pair<String,String> ... J ) {
		this( Arrays.asList(J) );
	}

	public MappingOpJoin( final List<Pair<String,String>> J ) {
		assert J != null;
		this.J = J;
	}

	public List<Pair<String,String>> getJ() { return J; }

	@Override
	public int getExpectedNumberOfSubExpressions() { return 2; }

	@Override
	public void visit( final MappingOperatorVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public int hashCode() {
		return J.hashCode();
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return o instanceof MappingOpJoin j  &&  j.J.equals(J);
	}

	@Override
	public String toString() {
		return "join(" + J.toString() + ")";
	}
}
