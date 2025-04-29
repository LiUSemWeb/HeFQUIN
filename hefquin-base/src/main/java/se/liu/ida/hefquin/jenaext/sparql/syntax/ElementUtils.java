package se.liu.ida.hefquin.jenaext.sparql.syntax;

import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
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
			group.addElementFilter(f);
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

}
