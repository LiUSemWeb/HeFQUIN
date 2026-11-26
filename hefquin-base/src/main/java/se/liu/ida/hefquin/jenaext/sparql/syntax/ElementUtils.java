package se.liu.ida.hefquin.jenaext.sparql.syntax;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

/**
 * This class provides useful functionality related to Jena's
 * representation of graph patterns as {@link Element} objects.
 */
public class ElementUtils
{
	/**
	 * Appends the given expression as a filter at the end of the graph
	 * pattern represented by the given {@link Element} object.
	 */
	public static Element merge( final Expr expr, final Element elmt ) {
		// Create a new ElementGroup object, add into it the Element represented
		// by the given graph pattern, add into it the filters, and create a new
		// graph pattern from it.
		final ElementGroup group = new ElementGroup();

		// - convert the given graph pattern into an Element and add it to the group
		if ( elmt instanceof ElementGroup eg ) {
			// If the given graph pattern was converted to an ElementGroup, instead
			// of simply adding it into the new group, copy its sub-elements over
			// to the new group, which avoids unnecessary nesting of groups.
			for ( final Element subElmt : eg.getElements() ) {
				group.addElement(subElmt);
			}
		}
		else {
			// If the given graph pattern was converted to something other
			// than an ElementGroup, simply add it into the new group.
			group.addElement(elmt);
		}

		// - now add the filter to the group
		final ElementFilter f = new ElementFilter(expr);
		group.addElement(f);

		return group;
	}
	/**
	 * Merges the given expressions as filters into the graph
	 * pattern represented by the given {@link Element} object.
	 */
	public static Element merge( final ExprList exprs, final Element elmt ) {
		// Create a new ElementGroup object, add into it the Element represented
		// by the given graph pattern, add into it the filters, and create a new
		// graph pattern from it.
		final ElementGroup group = new ElementGroup();

		// - convert the given graph pattern into an Element and add it to the group
		if ( elmt instanceof ElementGroup eg ) {
			// If the given graph pattern was converted to an ElementGroup, instead
			// of simply adding it into the new group, copy its sub-elements over
			// to the new group, which avoids unnecessary nesting of groups.
			for ( final Element subElmt : eg.getElements() ) {
				group.addElement(subElmt);
			}
		}
		else {
			// If the given graph pattern was converted to something other
			// than an ElementGroup, simply add it into the new group.
			group.addElement(elmt);
		}

		// - now add the filters to the group
		for ( final Expr expr : exprs ) {
			final ElementFilter f = new ElementFilter(expr);
			group.addElement(f);
		}

		return group;
	}

	/**
	 * Merges the given expressions as BIND clauses into the graph
	 * pattern represented by the given {@link Element} object.
	 */
	public static Element merge( final VarExprList exprs, final Element elmt ) {
		// Create a new ElementGroup object, add into it the Element represented
		// by the given graph pattern, add into it the filters, and create a new
		// graph pattern from it.
		final ElementGroup group = new ElementGroup();

		// - convert the given graph pattern into an Element and add it to the group
		if ( elmt instanceof ElementGroup eg ) {
			// If the given graph pattern was converted to an ElementGroup, instead
			// of simply adding it into the new group, copy its sub-elements over
			// to the new group, which avoids unnecessary nesting of groups.
			for ( final Element subElmt : eg.getElements() ) {
				group.addElement(subElmt);
			}
		}
		else {
			// If the given graph pattern was converted to something other
			// than an ElementGroup, simply add it into the new group.
			group.addElement(elmt);
		}

		// - now add the BIND clause to the group
		for ( final Var v : exprs.getVars() ) {
			final ElementBind f = new ElementBind(v, exprs.getExpr(v) );
			group.addElement(f);
		}

		return group;
	}

	/**
	 * Merges the given triple pattern into the graph pattern represented by
	 * the given {@link Element} object, using join semantics.
	 */
	public static Element merge( final Triple tp, final Element elmt ) {
		// If we can still create a BGP, then we do that.
		if ( elmt instanceof ElementTriplesBlock block ) {
			// Create the BGP and add the given triple pattern into it
			final ElementTriplesBlock bgp = new ElementTriplesBlock();
			bgp.addTriple(tp);

			// add the triple patterns from the given graph pattern into the BGP as well
			final Iterator<Triple> it = block.patternElts();
			while ( it.hasNext() ) {
				bgp.addTriple( it.next() );
			}

			return bgp;
		}

		// At this point it is clear that we will return a GenericSPARQLGraphPatternImpl1,
		// for which we need to create an Element object first. The type of Element that
		// we create depends on the type of the Element that the given graph pattern was
		// converted to.
		final Element resultElmt;

		if ( elmt instanceof ElementPathBlock block ) {
			// If the given graph pattern was converted to an ElementPathBlock,
			// create a copy of that ElementPathBlock and add the given triple
			// pattern into that copy.
			final ElementPathBlock copy = new ElementPathBlock();

			final Iterator<TriplePath> it = block.patternElts();
			while ( it.hasNext() ) {
				copy.addTriple( it.next() );
			}

			copy.addTriple(tp);

			resultElmt = copy;
		}
		else if ( elmt instanceof ElementGroup eg ) {
			// If the given graph pattern was converted to an ElementGroup,
			// create a copy of that ElementGroup with the same sub-elements.
			// When creating the copy, try to add the given triple pattern
			// into a copy of one of the sub-elements. If that's not possible
			// (i.e., none of the sub-elements is of a suitable type), then
			// add the triple pattern as an additional sub-element to the copy
			// in the end.
			final ElementGroup newGroup = new ElementGroup();
			boolean tpAdded = false;
			for ( final Element subElmt : eg.getElements() )
			{
				if ( ! tpAdded && subElmt instanceof ElementTriplesBlock block ) {
					final ElementTriplesBlock copy = new ElementTriplesBlock();

					final Iterator<Triple> it = block.patternElts();
					while ( it.hasNext() ) {
						copy.addTriple( it.next() );
					}

					copy.addTriple(tp);
					tpAdded = true;

					newGroup.addElement(copy);
				}
				else if ( ! tpAdded && subElmt instanceof ElementPathBlock block ) {
					final ElementPathBlock copy = new ElementPathBlock();

					final Iterator<TriplePath> it = block.patternElts();
					while ( it.hasNext() ) {
						copy.addTriplePath( it.next() );
					}

					copy.addTriple(tp);
					tpAdded = true;

					newGroup.addElement(copy);
				}
				else {
					newGroup.addElement(subElmt);
				}
			}

			if ( ! tpAdded ) {
				final ElementTriplesBlock bgp = new ElementTriplesBlock();
				bgp.addTriple(tp);
				newGroup.addElement(bgp);
			}

			resultElmt = newGroup;
		}
		else {
			// In all other cases, create an ElementGroup, ...
			final ElementGroup newGroup = new ElementGroup();

			// ... add the Element obtained for the given graph
			// pattern as one sub-element of the group, and ...
			newGroup.addElement(elmt);

			// ... add the given triple pattern as another sub-element.
			final ElementTriplesBlock bgp = new ElementTriplesBlock();
			bgp.addTriple(tp);
			newGroup.addElement(bgp);

			resultElmt = newGroup;
		}

		return resultElmt;
	}

	/**
	 * If the given list contains multiple elements, this function
	 * returns an {@link ElementGroup} containing these elements.
	 * If the list contains a single element only, this element
	 * is returned.
	 */
	public static Element createElementGroupIfNeeded( final List<Element> elmts ) {
		if ( elmts.size() == 1 )
			return elmts.get(0);

		final ElementGroup eg = new ElementGroup();
		for ( final Element e : elmts ) {
			eg.addElement(e);
		}

		return eg;
	}

	/**
	 * Returns the given element with the specified filter expressions removed.
	 * If the element is an {@link ElementGroup}, its immediate filter elements
	 * are processed. If the element is an {@link ElementFilter}, its expressions
	 * are processed directly. Expressions combined using logical AND are removed
	 * individually, preserving any expressions not specified for removal.
	 * Other element types are preserved without further traversal.
	 *
	 * @return the resulting element, or {@code null} if the element is a filter
	 *         whose expressions have all been removed
	 */
	public static Element removeFilters( final Element element,
	                                     final Set<Expr> filtersExpressionsToRemove )
	{
		if ( element instanceof ElementGroup elementGroup ) {
			// Avoid creating a new group if there are no filters to process.
			if ( ! containsFilter(elementGroup) )
				return element;

			final ElementGroup group = new ElementGroup();

			for ( final Element subElement : elementGroup.getElements() ) {
				if ( subElement instanceof ElementFilter filter ) {
					final ElementFilter remainingFilter = createRemainingFilter(filter, filtersExpressionsToRemove);
					if ( remainingFilter != null )
						group.addElement(remainingFilter);
				}
				else if ( subElement instanceof ElementGroup ) {
					final Element remainingElement = removeFilters(subElement, filtersExpressionsToRemove);
					if ( remainingElement != null )
						group.addElement(remainingElement);
				}
				else {
					group.addElement(subElement);
				}
			}
			return group.isEmpty() ? null : group;
		}
		else if ( element instanceof ElementFilter filter )
			return createRemainingFilter(filter, filtersExpressionsToRemove);
		else
			return element;
	}

	/**
	 * Creates a filter containing only the expressions that are not specified
	 * for removal. Expressions combined using logical AND are processed
	 * individually.
	 *
	 * @return the original filter if no expressions were removed, a new filter
	 *         containing the remaining expressions, or {@code null} if all
	 *         expressions were removed
	 */
	public static ElementFilter createRemainingFilter( final ElementFilter filter,
	                                                   final Set<Expr> filtersExpressionsToRemove ) {
		final ExprList remainingExprs = collectRemainingExpressions( filter.getExpr(), filtersExpressionsToRemove );

		if ( remainingExprs == null )
			return filter;

		if ( remainingExprs.isEmpty() )
			return null;

		final Expr remainingExpr = remainingExprs.size() == 1
				? remainingExprs.get(0)
				: remainingExprs.getList().stream()
						.reduce((a, b) -> new E_LogicalAnd(a, b))
						.orElseThrow();

		return new ElementFilter(remainingExpr);
	}

	/**
	 * Checks whether the given element or any of its nested groups contains a filter.
	 *
	 * @param element the element to inspect
	 * @return {@code true} if the element or one of its nested groups contains a filter
	 */
	private static boolean containsFilter( final Element element ) {
		if ( element instanceof ElementFilter )
			return true;

		if ( element instanceof ElementGroup group )
			return group.getElements().stream().anyMatch(ElementUtils::containsFilter);

		return false;
	}

	/**
	 * Collects the expressions from the given filter expression that are not
	 * contained in the set of expressions to remove.
	 *
	 * @param expr            the filter expression to process
	 * @param filtersToRemove the expressions to remove
	 * @return {@code null} if no expressions were removed; otherwise, the remaining
	 *         expressions, which may be empty if all expressions were removed
	 */
	private static ExprList collectRemainingExpressions(
			final Expr expr,
			final Set<Expr> filtersToRemove )
	{
		final ExprList remainingExprs = new ExprList();
		collectRemainingExpressionsInto( expr, filtersToRemove, remainingExprs );

		return countExpressions(expr) == remainingExprs.size()
				? null
				: remainingExprs;
	}

	private static ExprList collectRemainingExpressionsInto(
			final Expr expr,
			final Set<Expr> filtersToRemove,
			final ExprList remainingExprs
		 )
	{
		if ( expr instanceof E_LogicalAnd logicalAnd ) {
			remainingExprs.addAll( collectRemainingExpressionsInto( logicalAnd.getArg1(), filtersToRemove, remainingExprs ) );
			remainingExprs.addAll( collectRemainingExpressionsInto( logicalAnd.getArg2(), filtersToRemove, remainingExprs ) );
		}
		else if ( ! filtersToRemove.contains(expr) ) {
			remainingExprs.add(expr);
		}
		return remainingExprs;
	}

	private static int countExpressions( final Expr expr ) {
		if ( expr instanceof E_LogicalAnd logicalAnd )
			return countExpressions(logicalAnd.getArg1()) + countExpressions(logicalAnd.getArg2());

		return 1;
	}

}
