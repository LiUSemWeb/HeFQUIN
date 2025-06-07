package se.liu.ida.hefquin.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.PatternVarsVisitor;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCleanGroupsOfOne;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;

import se.liu.ida.hefquin.jenaext.PatternVarsAll;
import se.liu.ida.hefquin.jenaext.sparql.syntax.ElementUtils;

/**
 * Queries with a WHERE clause of a form such as the following one need to be
 * rewritten, which is what this class does.
 *
 * <pre>
 * PREFIX ex: <http://example.org/>
 *
 * SELECT * WHERE {
 *   VALUES (?s1 ?s2) {
 *     (ex:endpoint1 ex:endpoint2)
 *     (ex:endpoint1 ex:endpoint3)
 *   }
 *   SERVICE ?s1 { .. some pattern (that neither mentions ?s1 nor ?2) .. }
 *   SERVICE ?s2 { .. also some pattern (that also doesn't mention ?s1 or ?2) .. }
 * }
 * </pre>
 *
 * The rewriting that is applied to such query patterns is to apply the
 * solution mappings of the VALUES clause to the SERVICE clauses and, then
 * remove the VALUES clause. If the VALUES clause contains multiple solution
 * mappings (as in the example above), than the group of SERVICE clauses is
 * copied for each of these solution mappings, and the resulting groups are
 * combined via UNION. Additionally, in any of the two cases (just one solution
 * mapping in the VALUES clause or multiple), BIND clauses are appended to still
 * capture the bindings for the variables of the VALUES clause. For instance,
 * the result of rewriting the example query above is given as follows.
 *
 * <pre>
 * PREFIX ex: <http://example.org/>
 *
 * SELECT * WHERE {
 *   {
 *     SERVICE ex:endpoint1 { .. some pattern (that neither mentions ?s1 nor ?2) .. }
 *     SERVICE ex:endpoint2 { .. also some pattern (that also doesn't mention ?s1 or ?2) .. }
 *     BIND (?s1 AS ex:endpoint1)
 *     BIND (?s2 AS ex:endpoint2)
 *   }
 *   UNION
 *   {
 *     SERVICE ex:endpoint1 { .. some pattern (that neither mentions ?s1 nor ?2) .. }
 *     SERVICE ex:endpoint3 { .. also some pattern (that also doesn't mention ?s1 or ?2) .. }
 *     BIND (?s1 AS ex:endpoint1)
 *     BIND (?s2 AS ex:endpoint3)
 *   }
 * }
 * </pre>
 *
 * The VALUES clause may also be split up in the given queries, in order to
 * avoid a combinatorial blow-up of possible combinations. For instance, the
 * the initial example query above may also be provided in the following form
 * (which results in the same rewritten query).
 *
 * <pre>
 * PREFIX ex: <http://example.org/>
 *
 * SELECT * WHERE {
 *   VALUES ?s1 { ex:endpoint1 }
 *   VALUES ?s2 { ex:endpoint2  ex:endpoint3 }
 *   SERVICE ?s1 { .. some pattern (that neither mentions ?s1 nor ?2) .. }
 *   SERVICE ?s2 { .. also some pattern (that also doesn't mention ?s1 or ?2) .. }
 * }
 * </pre>
 *
 * When using multiple VALUES clauses, it is not even necessary to place all
 * of them at the beginning of the WHERE clause. Instead, some of them may be
 * moved closer to the SERVICE clause(s) in which the variables of a VALUES
 * clause are used as the service variable. For instance, the initial example
 * query above may also be provided in the following form (but, then, the
 * rewritten version looks different, as discussed below).
 *
 * <pre>
 * PREFIX ex: <http://example.org/>
 *
 * SELECT * WHERE {
 *   VALUES ?s1 { ex:endpoint1 }
 *   SERVICE ?s1 { .. some pattern (that neither mentions ?s1 nor ?2) .. }
 *   VALUES ?s2 { ex:endpoint2  ex:endpoint3 }
 *   SERVICE ?s2 { .. also some pattern (that also doesn't mention ?s1 or ?2) .. }
 * }
 * </pre>
 *
 * However, queries given in this form (with VALUES clauses in between other
 * patterns) are treated slightly different by the rewriting algorithm in this
 * class. That is, every VALUES clause that follows some other patterns (such
 * as the VALUES clause with variable <code>?s2</code> in the previous example)
 * is considered to end the scope of rewriting based on the previous VALUES
 * clause(s) and to start a new rewriting scope. The rewritten parts from the
 * different rewriting scopes are joined together in the resulting rewritten
 * query. For instance, for the previous example query, the result looks as
 * follows.
 *
 * <pre>
 * PREFIX ex: <http://example.org/>
 *
 * SELECT * WHERE {
 *   {
 *     SERVICE ex:endpoint1 { .. some pattern (that neither mentions ?s1 nor ?2) .. }
 *     BIND (?s1 AS ex:endpoint1)
 *   }
 *   {
 *     SERVICE ex:endpoint2 { .. also some pattern (that also doesn't mention ?s1 or ?2) .. }
 *     BIND (?s2 AS ex:endpoint2)
 *   }
 *   UNION
 *   {
 *     SERVICE ex:endpoint3 { .. also some pattern (that also doesn't mention ?s1 or ?2) .. }
 *     BIND (?s2 AS ex:endpoint3)
 *   }
 * }
 * </pre>
 *
 * Notice that this resulting query differs from the one obtained for the
 * cases in which the VALUES clauses are all at the beginning of the given
 * query. Here, each rewriting scope results in its own UNION group (instead
 * of just one big UNION group for everything together). Yet, since the input
 * queries are semantically equivalent, the same also hold for the two resulting
 * queries.
 *
 * The approach to consider different rewriting scopes introduces a limitation:
 * It does not support SERVICE clauses with a variable that is not bound by the
 * VALUES clause(s) that come closest before the SERVICE clause. Hence, HeFQUIN
 * cannot support queries with such SERVICE clauses.
 */
public class ValuesServiceQueryResolver
{
	/**
	 * If the WHERE clause of the given query is of a form that should be
	 * rewritten, then this method replaces the WHERE clause of the query
	 * by the rewritten one. Otherwise, the WHERE clause of the query is
	 * not changed.
	 *
	 * An {@link UnsupportedQueryException} is thrown if it is discovered
	 * that the WHERE clause of the given query uses VALUES clauses and
	 * SERVICE clauses in a way that is currently not supported.
	 *
	 * An {@link IllegalQueryException} is thrown if it is discovered
	 * that the WHERE clause of the given query uses VALUES clauses in an
	 * incorrect way.
	 */
	public static void expandValuesPlusServicePattern( final Query q )
			throws UnsupportedQueryException, IllegalQueryException
	{
		if ( q.getQueryPattern() instanceof ElementGroup eg ) {
			// If the query pattern does not have at least two elements, there
			// is nothing to do here (because we need at least a VALUES clause
			// and a SERVICE clause to do something).
			if ( eg.size() < 2 )
				return;

			// Rewrite the elements of the query pattern.
			final List<Element> newElmts;
			try {
				newElmts = expandValuesPlusServicePattern( eg.getElements() );
			}
			catch ( final MyUnsupportedQueryException e ) {
				throw new UnsupportedQueryException( q, e.getMessage(), e );
			}
			catch ( final MyIllegalQueryException e ) {
				throw new IllegalQueryException( q, e.getMessage(), e );
			}

			// If the attempt to rewrite the elements of the query
			// pattern did not result in any change (as indicated
			// by a null value), there is nothing to be done here.
			if ( newElmts == null )
				return;

			// Safety check.
			assert newElmts.size() > 0;

			final Element newQueryPattern;
			if ( newElmts.size() == 1 ) {
				// If rewriting the elements of the query pattern resulted
				// in a single new element, use that element as the new
				// query pattern.
				newQueryPattern = newElmts.get(0);
			}
			else {
				// If rewriting the elements of the query pattern resulted
				// in multiple new elements, gorup them together as the new
				// query pattern.
				final ElementGroup newGroup = new ElementGroup();
				for ( final Element elmt : newElmts ) {
					newGroup.addElement(elmt);
				}
				newQueryPattern = newGroup;
			}

			// Clean up groups of one in the new query pattern (if any) and ...
			final ElementTransform t = new ElementTransformCleanGroupsOfOne();
			final Element newQueryPattern2 = ElementTransformer.transform(newQueryPattern, t);
			// ... establish the result as the new query pattern.
			q.setQueryPattern(newQueryPattern2);
		}
		else {
			// We are not expecting the query pattern to be
			// anything else than a group graph pattern.
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Rewrites the given list of query elements (if needed). Returns
	 * <code>null</code> if it turns out that nothing needs to be rewritten,
	 * which is the case if there is no VALUES clause in the given list or
	 * there is a VALUES clause but only as the very last element of the list.
	 *
	 * Throws an {@link MyIllegalQueryException} if it discovers two
	 * VALUES clauses whose sets of variables are not disjoint.
	 */
	protected static List<Element> expandValuesPlusServicePattern( final List<Element> elmts )
			throws MyUnsupportedQueryException, MyIllegalQueryException
	{
		// Determine the position of the first VALUES clause in the given list.
		final int posFirstValuesClause = positionOfNextVALUES(elmts, 0);

		// If there is no VALUES clause in the list, then there is no need to
		// expand anything, which we indicate by returning null. Likewise, if
		// the first VALUES clause is actually the last element of the list,
		// then there is also no need to expand anything.
		if ( posFirstValuesClause == -1 || posFirstValuesClause == elmts.size()-1 )
			return null;

		// Rewrite the remaining query elements after the first VALUES clause
		// (by using the solution mappings of the VALUES clause as a basis).
		final ElementData valClause = (ElementData) elmts.get(posFirstValuesClause);
		final List<Element> rewrittenRemainder = expandValuesPlusServicePattern(elmts, valClause, posFirstValuesClause+1);

		// If the remaining query elements have not been changed, return null
		// to indicate that nothing has changed for the entire list of query
		// elements that was given to this function.
		if ( rewrittenRemainder == null ) {
			return null;
		}

		// If the remaining query elements have been changed and the first
		// VALUES clause (based on which they have been changed) is the first
		// element of the given list of query elements, then the rewritten
		// version of the remaining elements is the result of this function.
		if ( posFirstValuesClause == 0 ) {
			return rewrittenRemainder;
		}

		// At this point we have the case that the remaining query elements
		// have been changed and the first VALUES clause is *not* the first
		// element of the given list of query elements. In this case, the
		// result is a list that contains all the elements that the given
		// list contains before the first VALUE clause, ...
		final List<Element> newElmts = new ArrayList<>();
		for ( int i = 0; i < posFirstValuesClause; i++ ) {
			newElmts.add( elmts.get(i) );
		}
		// ... together with the rewritten version of the remaining elements.
		newElmts.addAll(rewrittenRemainder);
		return newElmts;
	}

	/**
	 * Rewrites the given list of query elements by using the solution
	 * mappings of the given VALUES clause as a basis, and starting only
	 * from the given position within the list (i.e., list elements that
	 * are at an earlier position in the list are ignored).
	 * May return <code>null</code>, namely in case that given position
	 * is the very last position of the list and there is another VALUES
	 * clause at that position.
	 *
	 * Throws an {@link MyIllegalQueryException} if it discovers two
	 * VALUES clauses whose sets of variables are not disjoint.
	 */
	protected static List<Element> expandValuesPlusServicePattern( final List<Element> elmts,
	                                                               final ElementData valClause,
	                                                               final int startPos )
			throws MyUnsupportedQueryException, MyIllegalQueryException
	{
		// First, handle the case that the element at the given start
		// position is a VALUES clause as well.
		if ( elmts.get(startPos) instanceof ElementData valClause2 ) {
			// If the element at the given start position is a VALUES clause
			// but it is also the last element of the given list, then there
			// is nothing left to rewrite anymore, which we indicate by
			// returning null.
			if ( startPos + 1 == elmts.size() )
				return null;

			// Otherwise, merge that VALUES clause with the given VALUES clause
			// (by creating a cross-product of their respective sets of solution
			// mappings), and ...
			final ElementData mergedValClause = merge(valClause, valClause2);

			// ... call this function recursively with the merged VALUES clause.
			return expandValuesPlusServicePattern(elmts, mergedValClause, startPos+1);
		}

		// Check that none of the variables bound by the given VALUES clause is
		// mentioned anywhere else except as the variable of a SERVICE clause.
		//   i) collect all the variables mentioned anywhere else
		final PatternVarsVisitor varCollect = new PatternVarsVisitorWithoutServiceVars( new HashSet<>() );
		for ( int i = startPos+1; i < elmts.size(); i++ ) {
			ElementWalker.walk( elmts.get(i), varCollect );
		}
		//   ii) and check that none of them is bound by the given VALUES clause
		for ( final Var v : valClause.getVars() ) {
			if ( varCollect.acc.contains(v) )
				throw new MyUnsupportedQueryException("HeFQUIN does not support VALUES clauses with variables that are mentioned anywhere else than as the variable of a SERVICE clause (which is not the case for variable ?" + v.getVarName() + ").");
		}

		// Determine the position of the next VALUES clause in the given
		// list, starting from the given start position + 1 (+ 1 because
		// the element at the start position is not a VALUES clause;
		// otherwise, we would have ended up in the if-block above).
		final int posNextValuesClause = positionOfNextVALUES(elmts, startPos+1);

		// Check that none of the elements in scope of the given VALUES clause
		// is, or contains, a SERVICE clause with a service variable that is not
		// one of the variables mentioned by the given VALUES clause.
		// (The scope ends either with the element before the next VALUES clause
		// or, if there is no next VALUES clause, with the last element of the
		// given list.)
		final int endOfScope = ( posNextValuesClause == -1 ) ? elmts.size()-1 : posNextValuesClause-1;
		final VisitorToCheckServiceVariables vis = new VisitorToCheckServiceVariables( valClause.getVars() );
		for ( int i = startPos; i <= endOfScope; i++ ) {
			ElementWalker.walk( elmts.get(i), vis );
			if ( vis.getDiscoveredVar() != null )
				throw new MyUnsupportedQueryException("HeFQUIN does not support SERVICE clauses with a variable that is not bound by the VALUES clause that comes closest before the SERVICE clause (which is not the case for variable ?" + vis.getDiscoveredVar().getVarName() + ").");
		}

		// Rewrite the elements that are in scope of the given VALUES clause
		// (by using the solution mappings of the VALUES clause as a basis).
		final List<Element> rewrittenValuesScope = rewrite(valClause, elmts, startPos, endOfScope );

		// Create the resulting list of elements, for which we need to consider
		// two cases: either there is no next VALUES clause or there is.
		final List<Element> result;
		if ( posNextValuesClause == -1 ) {
			// If there is no next VALUES clause, the result is a singleton
			// list that contains the element obtained by rewriting the
			// elements that are in scope of the given VALUES clause.
			result = rewrittenValuesScope;
		}
		else {
			// If there is a next VALUES clause, the result is a list whose
			// first element is the element obtained by rewriting the elements
			// that are in scope of the given VALUES clause, followed by the
			// list of elements obtained by calling this function recursively
			// for the next VALUES clause.
			result = expandValuesPlusServicePattern( elmts,
			                                         (ElementData) elmts.get(posNextValuesClause),
			                                         posNextValuesClause+1 );
			result.addAll(0, rewrittenValuesScope);
		}

		return result;
	}

	/**
	 * Determines the position of the next VALUES clause in the given
	 * list of query elements, starting from the given position (i.e.,
	 * the first element that this function considers is the element at
	 * the given position). If there is no VALUES clause in the given
	 * list from the given position until the end of the list, then this
	 * function returns -1.
	 */
	protected static int positionOfNextVALUES( final List<Element> elmts, final int startPos ) {
		// We simply iterate over the list, starting from the
		// given position, until we find a VALUES clause.
		int i = startPos - 1;
		while( ++i < elmts.size() ) {
			if ( elmts.get(i) instanceof ElementData )
				// VALUES clause found. Return the position of it.
				return i;
		}

		// No VALUES clause found.
		return -1;
	}

	/**
	 * Merges the two given VALUES clauses into a single one by creating
	 * a cross-product of their respective sets of solution mappings.
	 *
	 * Throws an {@link MyIllegalQueryException} if the sets of variables
	 * of the two given VALUES clauses are not disjoint.
	 */
	protected static ElementData merge( final ElementData valClause1,
	                                    final ElementData valClause2 )
			throws MyIllegalQueryException
	{
		// Check that the sets of variables of the two given VALUES clauses
		// are not disjoint. If that is not the case, throw an exception,
		// mentioning one of the violating variables as an example to let
		// the user know what exactly needs to be fixed in their query.
		if ( ! Collections.disjoint(valClause1.getVars(), valClause2.getVars()) ) {
			for ( final Var v : valClause1.getVars() ) {
				if ( valClause2.getVars().contains(v) )
					throw new MyIllegalQueryException("The same variable(s) are defined in different VALUES clauses (e.g., ?" + v.getVarName()+ ")");
			}
		}

		// Create the list of variables for the resulting VALUES clause, which
		// contains all variables of both of the two given VALUES clauses.
		final List<Var> allVars = new ArrayList<>();
		allVars.addAll( valClause1.getVars() );
		allVars.addAll( valClause2.getVars() );

		// Create the set of solution mappings for the resulting VALUES clause,
		// which is the cross-product of the sets of solution mappings of the
		// two given VALUES clauses.
		final List<Binding> mergedSolMaps = new ArrayList<>();
		for ( final Binding b1 : valClause1.getRows() )
			for ( final Binding b2 : valClause2.getRows() )
				mergedSolMaps.add( BindingLib.merge(b1,b2) );

		return new ElementData(allVars, mergedSolMaps);
	}

	/**
	 * Rewrites the query elements of the given list based on the solution
	 * mappings of the given VALUES clause, but considering only the list
	 * elements from the given start position until (and including) the given
	 * end position. Assumes that none of these elements is a VALUES clause.
	 */
	protected static List<Element> rewrite( final ElementData valClause,
	                                        final List<Element> elmts,
	                                        final int startPos,
	                                        final int endPos ) {
		// First, rewrite the relevant list elements based on the
		// first solution mapping of the given VALUES clause.
		final Iterator<Binding> it = valClause.getRows().iterator();
		final List<Element> rewriteUsingFirstRow = rewrite( it.next(), elmts, startPos, endPos, valClause.getVars() );

		// If the given VALUES clause contains only one solution mapping,
		// we are done and can return the result of rewriting based on
		// that solution mapping.
		if ( ! it.hasNext() )
			return rewriteUsingFirstRow;

		// If the given VALUES clause contains more than one solution mapping,
		// then we need to combine the rewritings obtained based on each of
		// these solution mappings into a UNION pattern. The rewriting created
		// based on the first solution mapping becomes the first part of this
		// UNION pattern.
		final ElementUnion eu = new ElementUnion();
		eu.addElement( ElementUtils.createElementGroupIfNeeded(rewriteUsingFirstRow) );

		// Iterate over the remaining solution mappings of the given VALUES
		// clause, rewrite the relevant list elements based on each of them,
		// and add each of the resulting rewritings as another part of the
		// UNION clause.
		while ( it.hasNext() ) {
			final List<Element> rewriteUsingNextRow = rewrite( it.next(), elmts, startPos, endPos, valClause.getVars() );
			eu.addElement( ElementUtils.createElementGroupIfNeeded(rewriteUsingNextRow) );
		}

		final List<Element> result = new ArrayList<>();
		result.add(eu);
		return result;
	}

	/**
	 * Rewrites the query elements of the given list based on the given
	 * solution mapping, but considering only the list elements from the
	 * given start position until (and including) the given end position.
	 * Assumes that none of these elements is a VALUES clause.
	 *
	 * The given list of variables is used to add BIND clauses; in particular,
	 * for every variable in this list, if the given solution mapping covers
	 * the variable, then a BIND clause is added that assigns the variable to
	 * the corresponding RDF term of the solution mapping.
	 */
	protected static List<Element> rewrite( final Binding solmap,
	                                        final List<Element> elmts,
	                                        final int startPos,
	                                        final int endPos,
	                                        final List<Var> varsForBind ) {
		// Initialization of the element transformer that changes the SERVICE
		// clauses and of the group pattern into which the potentially rewritten
		// query elements will be added.
		final ElementTransform transform = new MyElementTransform(solmap);
		final List<Element> newElmts = new ArrayList<>();

		// Iterate over the relevant elements of the given list of query elements.
		for ( int i = startPos; i <= endPos; i++ ) {
			// Apply the transformer to the current query element.
			final Element eOld = elmts.get(i);
			final Element eNew = ElementTransformer.transform(eOld, transform);

			// If the new element is the empty group graph pattern, then the
			// transformer handled the special case of a SERVICE clause whose
			// service variable is not bound in the given solution mapping.
			// In this case, ...
			if ( eNew instanceof ElementGroup eNewGroup && eNewGroup.isEmpty() ) {
				// ... we do not need to add anything to the new group graph
				// pattern that we are populating here; i.e., we do not need
				// to do anything here.
			}
			else {
				// Otherwise, we add the new element to the new group graph pattern.
				newElmts.add(eNew);
			}
		}

		// Add BIND clauses for all bindings in the given VALUES row.
		for ( final Var v : varsForBind ) {
			final Node n = solmap.get(v);
			if ( n != null ) {
				final NodeValue nv = NodeValue.makeNode(n);
				newElmts.add( new ElementBind(v, nv) );
			}
		}

		return newElmts;
	}


	/**
	 * Collects all variables in a given {@link Element} except for
	 * variables that occur *only* as the variable of SERVICE clauses.
	 */
	protected static class PatternVarsVisitorWithoutServiceVars extends PatternVarsAll.MyPatternVarsVisitor {
		public PatternVarsVisitorWithoutServiceVars( final Collection<Var> s ) { super(s); }

		@Override
		public void visit( final ElementService e ) {
			// do nothing and, thus, explicitly ignore e.getServiceNode()
		}
	}

	/**
	 * This exception is used internally by this class. The function
	 * {@link ValuesServiceQueryResolver#expandValuesPlusServicePattern(Query)}
	 * changes it into an {@link UnsupportedQueryException} when caught.
	 */
	protected static class MyUnsupportedQueryException extends Exception {
		private static final long serialVersionUID = 8989008960497975684L;
		public MyUnsupportedQueryException( final String msg ) { super(msg); }
	}

	/**
	 * This exception is used internally by this class. The function
	 * {@link ValuesServiceQueryResolver#expandValuesPlusServicePattern(Query)}
	 * changes it into an {@link IllegalQueryException} when caught.
	 */
	protected static class MyIllegalQueryException extends RuntimeException {
		private static final long serialVersionUID = 2289002960497975684L;
		public MyIllegalQueryException( final String msg ) { super(msg); }
	}

	/**
	 * Checks for each visited SERVICE clause that has a service variable
	 * whether that variable is in a given list of permitted variables.
	 * If it comes across a service variable that is not in the list,
	 * then it remembers this variable, to be accessed via
	 * {@link VisitorToCheckServiceVariables#getDiscoveredVar()}.
	 */
	protected static class VisitorToCheckServiceVariables extends ElementVisitorBase {
		protected final List<Var> permittedVars;
		protected Var discoveredVar = null;

		/**
		 * Creates the visitor with the list of variables that are permitted
		 * as service variables.
		 */
		public VisitorToCheckServiceVariables( final List<Var> permittedVars ) {
			this.permittedVars = permittedVars;
		}

		@Override
		public void visit( final ElementService e ) {
			if ( e.getServiceNode().isVariable() ) {
				final Var v = Var.alloc( e.getServiceNode() );
				if ( ! permittedVars.contains(v) ) {
					discoveredVar = v;
				}
			}
		}

		/**
		 * The result may be <code>null</code>, indicating that all service
		 * variables that the visitor came across have been permitted ones.
		 */
		public Var getDiscoveredVar() {
			return discoveredVar;
		}
	}

	/**
	 * Transforms all SERVICE clauses that have a service variable by replacing
	 * this variable with the URI that a given solution mapping assigns to the
	 * variable. Throws an {@link MyIllegalQueryException} if the solution
	 * mapping assigns something else than a URI to such a service variable.
	 */
	protected static class MyElementTransform extends ElementTransformCopyBase {
		protected final Binding solmap;

		public MyElementTransform( final Binding solmap ) { this.solmap = solmap; }

		@Override
		public Element transform( final ElementService e, final Node sn, final Element inside ) {
			if ( ! sn.isVariable() ) return e;

			final Node newServiceNode = solmap.get( sn.getName() );
			if ( newServiceNode == null ) {
				// Special case: if the variable is unbound in the current row
				// of the VALUES clause, it means that the source assignment was
				// created for a query with OPTIONAL, the current SERVICE clause
				// is inside an OPTIONAL, and the current SERVICE clause is known
				// not to produce a compatible solution mapping.
				return new ElementGroup();
			}

			if ( ! newServiceNode.isURI() ) {
				final String typeNameForMsg = ( newServiceNode.isLiteral() ) ? "literal" : newServiceNode.getClass().getName();
				throw new MyIllegalQueryException("A VALUES clause can only assign IRIs to service variables. This is not the case for variable ?" + sn.getName() + ", which is assigned a " + typeNameForMsg + " (" + newServiceNode.toString(true)+ ").");
			}

			return new ElementService( newServiceNode, inside, e.getSilent() );
		}
	}

}
