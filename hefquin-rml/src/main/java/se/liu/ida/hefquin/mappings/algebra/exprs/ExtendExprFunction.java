package se.liu.ida.hefquin.mappings.algebra.exprs;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

public class ExtendExprFunction implements ExtendExpression
{
	protected final ExtensionFunction fct;
	protected final ExtendExpression[] subExpressions;

	protected Set<String> allMentionedAttrs = null;

	public ExtendExprFunction( final ExtensionFunction fct,
	                           final ExtendExpression ... subExpressions ) {
		assert fct != null;
		assert fct.isCorrectNumberOfArgument( subExpressions.length );

		this.fct = fct;
		this.subExpressions = subExpressions;
	}

	@Override
	public Set<String> getAllMentionedAttributes() {
		if ( allMentionedAttrs == null ) {
			allMentionedAttrs = determineAllMentionedAttributes();
		}

		return allMentionedAttrs;
	}

	protected Set<String> determineAllMentionedAttributes() {
		if ( subExpressions.length == 0 )
			return ExtendExprConstant.emptySetOfStrings;

		if ( subExpressions.length == 1 )
			return subExpressions[0].getAllMentionedAttributes();

		final Set<String> result = new HashSet<>();
		for ( int i = 0; i < subExpressions.length; i++ ) {
			result.addAll( subExpressions[i].getAllMentionedAttributes() );
		}

		return result;
	}

	@Override
	public Node evaluate( final Map<String, Node> assignment ) {
		if ( subExpressions.length == 0 ) {
			return fct.apply();
		}

		final Node[] args = new Node[ subExpressions.length ];
		for ( int i = 0; i < subExpressions.length; i++ ) {
			args[i] = subExpressions[i].evaluate(assignment);
		}

		return fct.apply(args);
	}

}
