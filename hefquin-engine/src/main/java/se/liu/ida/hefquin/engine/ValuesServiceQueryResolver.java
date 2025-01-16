package se.liu.ida.hefquin.engine;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
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
 *   SERVICE ?s1 { .. some pattern (that neither mention ?s1 nor ?2) .. }
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
 * mapping in the VALUES clause or multiple), BIND clauses are added to still
 * capture the bindings for the variables of the VALUES clause. For instance,
 * the result of rewriting the example query above is given as follows.
 *
 * <pre>
 * PREFIX ex: <http://example.org/>
 *
 * SELECT * WHERE {
 *   {
 *     SERVICE ex:endpoint1 { .. some pattern (that neither mention ?s1 nor ?2) .. }
 *     SERVICE ex:endpoint2 { .. also some pattern (that also doesn't mention ?s1 or ?2) .. }
 *     BIND (?s1 AS ex:endpoint1)
 *     BIND (?s2 AS ex:endpoint2)
 *   }
 *   UNION
 *   {
 *     SERVICE ex:endpoint1 { .. some pattern (that neither mention ?s1 nor ?2) .. }
 *     SERVICE ex:endpoint3 { .. also some pattern (that also doesn't mention ?s1 or ?2) .. }
 *     BIND (?s1 AS ex:endpoint1)
 *     BIND (?s2 AS ex:endpoint3)
 *   }
 * }
 * </pre>
 */
public class ValuesServiceQueryResolver
{
	/**
	 * If the WHERE clause of the given query is of a form that should be
	 * rewritten (as is checked by using {@link #isQueryToBeExpanded(Query)}),
	 * then this method replaces the WHERE clause of the query by the rewritten
	 * one. Otherwise, the WHERE clause of the query is not changed.
	 */
	public static void expandValuesPlusServicePattern( final Query q ) {
		if ( isQueryToBeExpanded(q) ) {
			final ElementGroup eg = (ElementGroup) q.getQueryPattern();
			final ElementData values = (ElementData) eg.get(0);

			final Iterator<Binding> it = values.getRows().iterator();
			final Element firstRowBasedRewrite = rewrite( eg, it.next() );

			final Element newQueryPattern;
			if ( ! it.hasNext() ) {
				newQueryPattern = firstRowBasedRewrite;
			}
			else {
				final ElementUnion eu = new ElementUnion();
				eu.addElement(firstRowBasedRewrite);

				while ( it.hasNext() ) {
					final Element nextRowBasedRewrite = rewrite( eg, it.next() );
					eu.addElement(nextRowBasedRewrite);
				}

				final ElementGroup newEG= new ElementGroup();
				newEG.addElement(eu);

				newQueryPattern = newEG;
			}

			final ElementTransform t = new ElementTransformCleanGroupsOfOne();
			final Element newQueryPattern2 = ElementTransformer.transform(newQueryPattern, t);
			q.setQueryPattern(newQueryPattern2);
		}
	}

	/**
	 * Returns <code>true</code> if the WHERE clause of the given query is of
	 * a form that should be rewritten. That is, the WHERE clause must begin
	 * with a VALUES clause, none of the variables bound by the VALUES clause
	 * is mentioned anywhere else except as the variable of a SERVICE clause,
	 * the VALUES clause must mention every variable that is a variable of a
	 * SERVICE clause, there must be at least one SERVICE clause with a variable,
	 * and there must not be any other VALUES clause in the WHERE clause.
	 */
	public static boolean isQueryToBeExpanded( final Query q ) {
		if ( !(q.getQueryPattern() instanceof ElementGroup) ) return false;

		final ElementGroup eg = (ElementGroup) q.getQueryPattern();

		// The WHERE clause must contain more than one pattern, ...
		if ( eg.size() < 2 ) return false;

		// ... and the first of these patterns must be a VALUES clause.
		if ( !(eg.get(0) instanceof ElementData) ) return false;

		final Iterator<Element> it = eg.getElements().iterator();
		final ElementData values = (ElementData) it.next();

		// Check that none of the variables bound by the
		// VALUES clause is mentioned anywhere else except
		// as the variable of a SERVICE clause.
		final PatternVarsVisitor varCollect = new PatternVarsVisitorWithoutServiceVars( new HashSet<>() );
		while ( it.hasNext() ) {
			ElementWalker.walk( it.next(), varCollect );
		}
		for ( final Var varInValues : values.getVars() ) {
			if ( varCollect.acc.contains(varInValues) ) return false;
		}

		// Check the SERVICE clauses in the rest of the pattern.
		final ElementChecker c = new ElementChecker(values);
		try {
			ElementWalker.walk(eg, c);
		}
		catch ( final UnsupportedQueryException e ) {
			return false;
		}

		// No SERVICE clause with variable in the query.
		if ( c.getVarsOfServiceClauses().isEmpty() ) return false;

		// Finally, check that the VALUES clause binds all the variables
		// used for the SERVICE clause either to a URI or not at all.
		for ( final Var v : c.getVarsOfServiceClauses() ) {
			for ( final Binding sm : values.getRows() ) {
				final Node n = sm.get(v);
				if ( n != null && ! n.isURI() ) return false;
			}
		}

		return true;
	}

	protected static Element rewrite( final ElementGroup eg, final Binding solmap ) {
		final Iterator<Element> it = eg.getElements().iterator();
		it.next(); // ignore the VALUES clause
		return rewrite(it, solmap);
	}

	protected static Element rewrite( final Iterator<Element> it, final Binding solmap ) {
		final ElementTransform transform = new MyElementTransform(solmap);
		final ElementGroup eg = new ElementGroup();
		while ( it.hasNext() ) {
			final Element eOld = it.next();
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
				eg.addElement(eNew);
			}
		}

		// add BIND clauses for all bindings in the given solution mapping
		solmap.forEach( (var,node) -> {
			final NodeValue nv = NodeValue.makeNode(node);
			eg.addElement( new ElementBind(var, nv) );
		} );

		return eg;
	}


	protected static class ElementChecker extends ElementVisitorBase {
		protected final ElementData valuesClause;
		protected final List<Var> varsInValues;
		protected final Set<Var> varsOfServiceClauses = new HashSet<>();

		public ElementChecker( final ElementData valuesClause ) {
			this.valuesClause = valuesClause;
			this.varsInValues = valuesClause.getVars();
		}

		public Set<Var> getVarsOfServiceClauses() {
			return varsOfServiceClauses;
		}

		@Override
		public void visit( final ElementData e ) {
			if ( e != valuesClause )
				throw new UnsupportedQueryException();
		}

		@Override
		public void visit( final ElementService e ) {
			// If the SERVICE clause has a variables instead of
			// an IRI, then we need to check that this variable
			// is among the variables of the VALUES clause.
			final Node n = e.getServiceNode();
			if ( n.isVariable() ) {
				final Var v = Var.alloc(n);
				if ( ! varsInValues.contains(v) )
					throw new UnsupportedQueryException();

				varsOfServiceClauses.add(v);
			}
		}
	}


	protected static class UnsupportedQueryException extends RuntimeException {
		private static final long serialVersionUID = -7979008960497975684L;
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

			if ( ! newServiceNode.isURI() ) throw new IllegalArgumentException();

			return new ElementService( newServiceNode, inside, e.getSilent() );
		}
	}

}
