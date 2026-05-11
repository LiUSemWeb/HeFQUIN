package se.liu.ida.hefquin.mappings.algebra.ops.extexprs;

import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;

public class ExtendExprAttribute implements ExtendExpression
{
	protected final String attribute;
	protected final Set<String> singletonSet;

	public ExtendExprAttribute( final String attribute ) {
		assert attribute != null;
		this.attribute = attribute;
		this.singletonSet = Set.of(attribute);
	}

	@Override
	public Set<String> getAllMentionedAttributes() { return singletonSet; }

	@Override
	public Node evaluate( final Map<String, Node> assignment ) {
		final Node n = assignment.get(attribute);
		return ( n != null) ? n : MappingRelation.errorNode;
	}

	@Override
	public int hashCode() {
		return attribute.hashCode();
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return     o instanceof ExtendExprAttribute e
		       &&  e.attribute.equals(attribute);
	}

	@Override
	public String toString() { return attribute; }
}
