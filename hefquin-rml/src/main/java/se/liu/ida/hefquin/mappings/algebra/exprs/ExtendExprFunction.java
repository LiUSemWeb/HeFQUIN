package se.liu.ida.hefquin.mappings.algebra.exprs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

public class ExtendExprFunction implements ExtendExpression
{
	protected final ExtensionFunction fct;
	protected final List<ExtendExpression> subExpressions;

	protected Set<String> allMentionedAttrs = null;

	public ExtendExprFunction( final ExtensionFunction fct,
	                           final ExtendExpression ... subExpressions ) {
		this( fct, Arrays.asList(subExpressions) );
	}

	public ExtendExprFunction( final ExtensionFunction fct,
	                           final List<ExtendExpression> subExpressions ) {
		assert fct != null;
		assert fct.isCorrectNumberOfArgument( subExpressions.size() );

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
		if ( subExpressions.isEmpty() )
			return ExtendExprConstant.emptySetOfStrings;

		if ( subExpressions.size() == 1 )
			return subExpressions.get(0).getAllMentionedAttributes();

		final Set<String> result = new HashSet<>();
		for ( final ExtendExpression subExpr : subExpressions ) {
			result.addAll( subExpr.getAllMentionedAttributes() );
		}

		return result;
	}

	@Override
	public Node evaluate( final Map<String, Node> assignment ) {
		if ( subExpressions.isEmpty() ) {
			return fct.apply();
		}

		final Node[] args = new Node[ subExpressions.size() ];
		int i = 0;
		for ( final ExtendExpression subExpr : subExpressions ) {
			args[i++] = subExpr.evaluate(assignment);
		}

		return fct.apply(args);
	}

}
