package se.liu.ida.hefquin.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCleanGroupsOfOne;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformApplyElementTransform;

public class ValuesServiceQueryResolver2
{
	public static void expandValuesPlusServicePattern( final Query q ) {
		if ( q.getQueryPattern() instanceof ElementGroup eg ) {
			// If the query pattern does not have at least two elements, there
			// is nothing to do here (because we need at least a VALUES clause
			// and a SERVICE clause to do something).
			if ( eg.size() > 1 ) {
				// Make a copy of the query pattern, in case
				// something goes wrong during the rewriting.
				final ElementGroup egCopy = deepCopyQueryPattern(eg);

				final List<Element> newElmts = expandValuesPlusServicePattern( eg.getElements() );
				if ( newElmts == null ) {
					// do nothing
				}
				else if ( newElmts.size() == 1 ) {
					q.setQueryPattern( newElmts.get(0) );
				}
				else {
					final ElementGroup newGroup = new ElementGroup();
					for ( final Element elmt : newElmts ) {
						newGroup.addElement(elmt);
					}
					q.setQueryPattern(newGroup);
				}
			}
		}
	}

	protected static ElementGroup deepCopyQueryPattern( final ElementGroup eg ) {
		final ElementTransform eltTr = new ElementTransformCopyBase(true);
		final ExprTransform exprTr = new ExprTransformApplyElementTransform(eltTr, true);
		return (ElementGroup) ElementTransformer.transform(eg, eltTr, exprTr);
	}

	protected static List<Element> expandValuesPlusServicePattern( final List<Element> elmts ) {
		// Determine the position of the first VALUES clause in the given list.
		final int posFirstValuesClause = positionOfNextVALUES(elmts, 0);

		// If there is no VALUES clause in the list, then there is no need to
		// expand anything, which we indicate by returning null. Likewise, if
		// the first VALUES clause is actually the last element of the list,
		// then there is also no need to expand anything.
		if ( posFirstValuesClause == -1 || posFirstValuesClause == elmts.size()-1 )
			return null;

		final ElementData values = (ElementData) elmts.get(posFirstValuesClause);
		expandValuesPlusServicePattern(elmts, values, posFirstValuesClause+1);
	}

	/**
	 * Goes over the elements in the given element group, starting from the
	 * given position, until it comes an element that represents a VALUES
	 * clause. Returns a list containing all elements that came before that
	 * VALUES clause (starting from the given position), in the order in
	 * which these elements are in the group. If there is no VALUES clause
	 * from the given position until the end of the group, then the returned
	 * list contains all elements of group from the given position until the
	 * end. If the element at the given position represents a VALUES clause,
	 * then this function returns <code>null</code> (instead of returning an
	 * empty list).
	 */
	protected static List<Element> collectElmtsUntilFirstVALUES( final ElementGroup eg,
	                                                             final int start ) {
		// Check whether the element at the given position is
		// a VALUES clause and return null if that is the case.
		final Element firstElmt = eg.get(start);
		if ( firstElmt instanceof ElementData )
			return null;

		// Otherwise, add that element to the list to be returned ..
		final List<Element> result = new ArrayList<>();
		result.add(firstElmt);

		// .. and iterate over the next elements until either a one of
		// them is a VALUES clause or the end of the group is reached.
		int i = start;
		while ( ++i < eg.size() ) {
			final Element nextElmt = eg.get(i);
			if ( nextElmt instanceof ElementData )
				// VALUES clause found. Return the list of
				// elements collected up to this point.
				return result;
		}

		// There was no VALUES clause.
		return result;
	}

	protected static int positionOfNextVALUES( final List<Element> elmts, final int start ) {
		int i = start - 1;
		while( ++i < elmts.size() ) {
			final Element nextElmt = elmts.get(i);
			if ( nextElmt instanceof ElementData )
				return i;
		}

		return -1;
	}

}
