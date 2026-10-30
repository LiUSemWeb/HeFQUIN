package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

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

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
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
	 * Collects filter expressions from a SPARQL graph pattern by delegating
	 * to the appropriate method for the underlying Jena representation.
	 */
	public static void collectFiltersFromPattern( final SPARQLGraphPattern pattern, final Set<Expr> filters ) {
		if ( pattern instanceof GenericSPARQLGraphPatternImpl1 pattern1 )
			collectFiltersFromPattern(pattern1.asJenaElement(), filters);
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl2 pattern2 )
			collectFiltersFromOp(pattern2.asJenaOp(), filters);
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
