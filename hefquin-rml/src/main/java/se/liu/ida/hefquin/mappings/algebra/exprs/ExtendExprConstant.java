package se.liu.ida.hefquin.mappings.algebra.exprs;

import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

public class ExtendExprConstant implements ExtendExpression
{
	public static final Set<String> emptySetOfStrings = Set.of();

	protected final Node constant;

	public ExtendExprConstant( final Node constant ) {
		assert constant != null;
		this.constant = constant;
	}

	@Override
	public Set<String> getAllMentionedAttributes() { return emptySetOfStrings; }

	@Override
	public Node evaluate( final Map<String, Node> assg ) { return constant; }

	@Override
	public String toString() { return constant.toString(); }
}
