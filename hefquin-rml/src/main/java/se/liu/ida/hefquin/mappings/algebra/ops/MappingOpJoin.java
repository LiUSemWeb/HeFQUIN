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
}
