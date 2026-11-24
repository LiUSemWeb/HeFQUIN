package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExtendAssign;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpUnfold;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.base.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;

/**
 * Utility methods for collecting filter expressions from SPARQL graph patterns
 * and their underlying Jena representations.
 *
 * <p>Logical AND expressions are decomposed into their individual expressions.
 * Traversal is limited to supported element and operator types, with special
 * handling for constructs such as UNION, OPTIONAL, and MINUS to avoid collecting
 * filters from branches that are not traversed.
 *
 * <p>Collected expressions are added to a caller-provided set.
 */
public class FilterCollectingUtils
{
	/**
	 * Collects filter expressions from the given SPARQL graph pattern and adds
	 * them to the supplied set.
	 *
	 * Filters are collected from generic graph patterns and recursively from
	 * group patterns. Triple patterns, BGPs, and union patterns are not inspected.
	 *
	 * @param pattern the graph pattern to inspect
	 * @param filters the set to which collected filter expressions are added
	 * @throws IllegalArgumentException if the pattern implementation is unsupported
	 */
	public static void collectFiltersFromPattern( final SPARQLGraphPattern pattern, final Set<Expr> filters ) {
		if ( pattern instanceof GenericSPARQLGraphPatternImpl1 pattern1 )
			collectFiltersFromPattern(pattern1.asJenaElement(), filters);
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl2 pattern2 )
			collectFiltersFromOp(pattern2.asJenaOp(), filters);
		else if ( pattern instanceof TriplePattern
		       || pattern instanceof BGP
		       || pattern instanceof SPARQLUnionPattern )
			return;
		else if ( pattern instanceof SPARQLGroupPattern groupPattern )
			groupPattern.getSubPatterns().forEach( subPattern -> collectFiltersFromPattern(subPattern, filters) );
		else
			throw new IllegalArgumentException( "Unsupported SPARQLGraphPattern implementation: " + pattern.getClass().getName() );
	}

	/**
	 * Collects filter expressions from the given SPARQL graph pattern.
	 *
	 * Filters are collected from generic graph patterns and recursively from
	 * group patterns. Patterns that are not inspected for filters, such as triple
	 * patterns, BGPs, and union patterns, return {@code null}.
	 *
	 * @param pattern the graph pattern to inspect
	 * @return the collected filter expressions, or {@code null} if the pattern
	 *         does not support filter collection
	 * @throws IllegalArgumentException if the pattern implementation is unsupported
	 */
	public static Set<Expr> collectFiltersFromPattern( final SPARQLGraphPattern pattern ) {
		if ( pattern instanceof GenericSPARQLGraphPatternImpl1
		  || pattern instanceof GenericSPARQLGraphPatternImpl2 ) {
			final Set<Expr> filters = new HashSet<>();
			collectFiltersFromPattern( pattern, filters );
			return filters;
		}
		else if ( pattern instanceof TriplePattern
		       || pattern instanceof BGP
		       || pattern instanceof SPARQLUnionPattern )
			return null;
		else if ( pattern instanceof SPARQLGroupPattern groupPattern ) {
			Set<Expr> filters = null;

			for ( final SPARQLGraphPattern subPattern : groupPattern.getSubPatterns() ) {
				final Set<Expr> subPatternFilters = collectFiltersFromPattern(subPattern);

				if ( subPatternFilters != null ) {
					if ( filters == null )
						filters = new HashSet<>();

					filters.addAll(subPatternFilters);
				}
			}
			return filters;
		}
		else
			throw new IllegalArgumentException( "Unsupported SPARQLGraphPattern implementation: " + pattern.getClass().getName() );
	}

	/**
	 * Collects filter expressions from a Jena algebra tree.
	 *
	 * Extracts expressions from OpFilter operators and splits logical AND
	 * expressions into individual expressions. Filters inside UNION branches
	 * are not collected, and only the left branch of an OPTIONAL (OpLeftJoin)
	 * is traversed.
	 */
	public static void collectFiltersFromOp( final Op op, final Set<Expr> filters ) {
		// Do not collect filters from the branches of a union.
		if ( op instanceof OpUnion ) {
			return;
		}
		// Only collect filters from the left branch of an optional.
		else if ( op instanceof OpLeftJoin leftJoin ) {
			collectFiltersFromOp(leftJoin.getLeft(), filters);
		}
		// Traverse only into the left sub-operator of MINUS.
		else if ( op instanceof OpMinus opMinus ) {
			collectFiltersFromOp(opMinus.getLeft(), filters);
		}
		// Traverse into the sub-operator of unary operators that are supported.
		else if ( op instanceof OpExtendAssign opExtendAssign ) {
			collectFiltersFromOp(opExtendAssign.getSubOp(), filters);
		}
		else if ( op instanceof OpUnfold opUnfold ) {
			collectFiltersFromOp(opUnfold.getSubOp(), filters);
		}
		else if ( op instanceof OpFilter opFilter ) {
			for ( final Expr expr : opFilter.getExprs() ) {
				collectFilterExpressions(expr, filters);
			}
			collectFiltersFromOp(opFilter.getSubOp(), filters);
		}
		// Traverse into all sub-operators of supported joins.
		else if ( op instanceof OpJoin opJoin ) {
			collectFiltersFromOp(opJoin.getLeft(), filters);
			collectFiltersFromOp(opJoin.getRight(), filters);
		}
		else if ( op instanceof OpSequence opSequence ) {
			for ( final Op subOp : opSequence.getElements() ) {
				collectFiltersFromOp(subOp, filters);
			}
		}
		// Other operator types are intentionally not traversed.
	}

	/**
	 * Collects filter expressions from a Jena SPARQL pattern element.
	 *
	 * Extracts expressions from {@link ElementFilter} elements, recursively
	 * examines the children of {@link ElementGroup} elements, and splits
	 * logical AND expressions into their individual expressions.
	 * Only {@link ElementFilter} and {@link ElementGroup} sub-elements are
	 * traversed; other element types are intentionally ignored to avoid
	 * collecting filters from nested constructs such as OPTIONAL and MINUS.
	 */
	public static void collectFiltersFromPattern( final Element element, final Set<Expr> filters ) {
		// Extract and split the filter condition from a SPARQL FILTER element.
		if ( element instanceof ElementFilter filter ) {
			collectFilterExpressions(filter.getExpr(), filters);
		}
		// Recursively inspect the elements of a SPARQL group pattern.
		else if ( element instanceof ElementGroup group ) {
			for ( final Element subElement : group.getElements() ) {
				if ( subElement instanceof ElementFilter || subElement instanceof ElementGroup )
					collectFiltersFromPattern(subElement, filters);
			}
		}
		// Additional SPARQL element types can be handled as needed.
	}

	/**
	 * Recursively decomposes logical AND expressions into their individual
	 * expressions and adds them to the given set.
	 *
	 * Expressions that are not logical AND expressions are added to the set
	 * as-is.
	 */
	public static void collectFilterExpressions( final Expr expr, final Set<Expr> filters ) {
		if ( expr instanceof E_LogicalAnd logicalAnd ) {
			collectFilterExpressions(logicalAnd.getArg1(), filters);
			collectFilterExpressions(logicalAnd.getArg2(), filters);
		}
		else {
			filters.add(expr);
		}
	}
}
