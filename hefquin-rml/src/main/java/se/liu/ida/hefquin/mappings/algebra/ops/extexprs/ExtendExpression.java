package se.liu.ida.hefquin.mappings.algebra.ops.extexprs;

import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

public interface ExtendExpression
{
	Set<String> getAllMentionedAttributes();

	Node evaluate( Map<String,Node> assignment );
}
