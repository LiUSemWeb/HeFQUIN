package se.liu.ida.hefquin.mappings.algebra.exprs;

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

}
