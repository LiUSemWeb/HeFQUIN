package se.liu.ida.hefquin.jenaext.sparql.expr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

import se.liu.ida.hefquin.base.data.mappings.TermMapping;

public class ExprUtils
{
	/**
	 * Expands the given expression according to an entity mapping.
	 * <p>
	 * If the expression is a constant URI, it is translated using the given
	 * lookup function. If multiple local terms exist, a corresponding expression
	 * is created for each of them. Expressions that are not constant URIs are
	 * returned unchanged as a singleton list. Non-constant expressions are only returned
	 * unchanged if they do not contain any mapped global URIs anywhere in their
	 * structure.
	 *
	 * @param e the expression to expand
	 * @param lookup lookup function that returns the local terms for a given
	 *               global entity
	 * @return the translated expressions
	 */
	public static List<Expr> expandExpressionUsingEntityMapping( final Expr e,
	                                                             final Function<Node, Set<Node>> lookup,
	                                                             final Predicate<Node> containsGlobalTerm ) {
		if ( e.isConstant() ) {
			final List<Expr> exprs = new ArrayList<>();
			final Set<Node> nodes = new HashSet<>();
			final Node n = e.getConstant().asNode();

			if ( ! n.isURI() )
				nodes.add(n);
			else {
				final Set<Node> mappings = lookup.apply(n);

				if ( mappings == null || mappings.isEmpty() )
					nodes.add(n);
				else
					nodes.addAll(mappings);
			}


			for ( final Node node : nodes ) {
				exprs.add(NodeValue.makeNode(node));
			}

			return exprs;
		}
		else if ( containsMappedGlobalTerm(e, containsGlobalTerm) ) {
			throw new UnsupportedOperationException(
				"Filter expression " + e + " cannot be rewritten"
			);
		}
		else return List.of(e);
	}

	/**
	 * Expands the given expression according to a schema mapping.
	 * <p>
	 * If the expression is a constant URI, it is translated using the given
	 * lookup function. If multiple local terms exist, a corresponding expression
	 * is created for each of them. Expressions that are not constant URIs are
	 * returned unchanged as a singleton list. Non-constant expressions are only returned
	 * unchanged if they do not contain any mapped global URIs anywhere in their
 	 * structure.
	 *
	 * @param e the expression to expand
	 * @param lookup lookup function that returns the term mappings for a given
	 *               global schema term
	 * @return the translated expressions
	 */
	public static List<Expr> expandExpressionUsingSchemaMapping( final Expr e,
	                                                             final Function<Node, Set<TermMapping>> lookup,
	                                                             final Predicate<Node> containsGlobalTerm ) {
		if ( e.isConstant() ) {
			final List<Expr> exprs = new ArrayList<>();
			final Set<Node> nodes = new HashSet<>();
			final Node n = e.getConstant().asNode();

			if ( ! n.isURI() )
				nodes.add(n);
			else {
				final Set<TermMapping> mappings = lookup.apply(n);

				if ( mappings != null && ! mappings.isEmpty() )
					for ( final TermMapping tm : mappings )
						nodes.addAll( tm.getLocalTerms() );

				if ( nodes.isEmpty() )
					nodes.add(n);
			}

			for ( final Node node : nodes ) {
				exprs.add(NodeValue.makeNode(node));
			}

			return exprs;
		}
		else if ( containsMappedGlobalTerm(e, containsGlobalTerm) ) {
			throw new UnsupportedOperationException(
				"Filter expression " + e + " cannot be rewritten"
			);
		}
		else return List.of(e);
	}

	/**
	 * Recursively checks whether an expression contains any URI that is
	 * affected by the current mapping.
	 * <p>
	 * This includes URIs appearing anywhere in the expression tree, not only
	 * top-level constants. Variables are ignored. Function arguments are
	 * traversed recursively.
	 *
	 * @param expr the expression to inspect
	 * @param isMapped predicate that returns true if a given URI is part of
	 *                 the global vocabulary mapping
	 * @return true if any mapped global URI is present in the expression
	 */
	private static boolean containsMappedGlobalTerm( final Expr expr, final Predicate<Node> isMapped )
	{
		if ( expr.isConstant() ) {
			final Node n = expr.getConstant().asNode();
			return n.isURI() && isMapped.test(n);
		}

		if ( expr.isVariable() ) {
			return false;
		}

		if ( expr.isFunction() ) {
			for ( final Expr arg : expr.getFunction().getArgs() ) {
				if ( containsMappedGlobalTerm(arg, isMapped) )
					return true;
			}
		}

		return false;
	}
}
