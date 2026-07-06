package se.liu.ida.hefquin.jenaext.sparql.expr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;

public class ExprUtils
{
	/**
	 * Collects all URI nodes that occur anywhere within the given expression.
	 *
	 * @param expr the expression to inspect
	 * @return the URI nodes contained in the expression
	 */
	public static Set<Node> collectURIs( final Expr expr ) {
		final Set<Node> uris = new HashSet<>();
		collectURIs( expr, uris );
		return uris;
	}

	private static void collectURIs( final Expr expr, final Set<Node> uris ) {
		if ( expr.isConstant() ) {
			final Node n = expr.getConstant().asNode();
			if ( n.isURI() )
				uris.add(n);
			return;
		}

		if ( expr.isFunction() )
			for ( final Expr arg : expr.getFunction().getArgs() )
				collectURIs( arg, uris );
	}

	/**
	 * Builds a left-associative logical OR expression from the given list of
	 * expressions.
	 *
	 * @param exprList the expressions to combine
	 * @return a logical OR expression over all given expressions
	 * @throws IllegalArgumentException if the list is null or empty
	 */
	public static Expr buildOr( final List<Expr> exprList ) {
		if ( exprList == null || exprList.isEmpty() ) {
			throw new IllegalArgumentException( "Empty OR list" );
		}

		Expr result = exprList.get(0);

		for ( int i = 1; i < exprList.size(); i++ ) {
			result = new E_LogicalOr( result, exprList.get(i) );
		}

		return result;
	}

	/**
	 * Builds a left-associative logical AND expression from the given list of
	 * expressions.
	 *
	 * @param exprList the expressions to combine
	 * @return a logical AND expression over all given expressions
	 * @throws IllegalArgumentException if the list is null or empty
	 */
	public static Expr buildAnd( final List<Expr> exprList ) {
		if ( exprList == null || exprList.isEmpty() ) {
			throw new IllegalArgumentException("Empty AND list");
		}

		Expr result = exprList.get(0);

		for ( int i = 1; i < exprList.size(); i++ ) {
			result = new E_LogicalAnd( result, exprList.get(i) );
		}

		return result;
	}

}
