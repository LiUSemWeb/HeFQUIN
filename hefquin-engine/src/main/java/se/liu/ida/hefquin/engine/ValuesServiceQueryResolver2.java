package se.liu.ida.hefquin.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.PatternVarsVisitor;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCleanGroupsOfOne;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformApplyElementTransform;

import se.liu.ida.hefquin.engine.ValuesServiceQueryResolver.MyElementTransform;
import se.liu.ida.hefquin.engine.ValuesServiceQueryResolver.PatternVarsVisitorWithoutServiceVars;
import se.liu.ida.hefquin.jenaext.PatternVarsAll;

public class ValuesServiceQueryResolver2
{
	public static void expandValuesPlusServicePattern( final Query q )
			throws UnsupportedQueryException
	{
		if ( q.getQueryPattern() instanceof ElementGroup eg ) {
			// If the query pattern does not have at least two elements, there
			// is nothing to do here (because we need at least a VALUES clause
			// and a SERVICE clause to do something).
			if ( eg.size() > 1 ) {
				// Make a copy of the query pattern, in case
				// something goes wrong during the rewriting.
				final ElementGroup egCopy = deepCopyQueryPattern(eg);

				final List<Element> newElmts;
				try {
					newElmts = expandValuesPlusServicePattern( eg.getElements() );
				}
				catch ( final MyUnsupportedQueryException e ) {
					throw new UnsupportedQueryException( q, e.getMessage(), e );
				}

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

	protected static List<Element> expandValuesPlusServicePattern( final List<Element> elmts )
			throws MyUnsupportedQueryException
	{
		// Determine the position of the first VALUES clause in the given list.
		final int posFirstValuesClause = positionOfNextVALUES(elmts, 0);

		// If there is no VALUES clause in the list, then there is no need to
		// expand anything, which we indicate by returning null. Likewise, if
		// the first VALUES clause is actually the last element of the list,
		// then there is also no need to expand anything.
		if ( posFirstValuesClause == -1 || posFirstValuesClause == elmts.size()-1 )
			return null;

		final ElementData values = (ElementData) elmts.get(posFirstValuesClause);
		final List<Element> rewrittenRemainder = expandValuesPlusServicePattern(elmts, values, posFirstValuesClause+1);
		if ( rewrittenRemainder == null ) {
			return null;
		}
		else if ( posFirstValuesClause == 0 ) {
			return rewrittenRemainder;
		}
		else {
			final List<Element> newElmts = new ArrayList<>();
			for ( int i = 0; i < posFirstValuesClause; i++ ) {
				newElmts.add( elmts.get(i) );
			}
			newElmts.addAll(rewrittenRemainder);
			return newElmts;
		}
	}

	protected static List<Element> expandValuesPlusServicePattern( final List<Element> elmts,
	                                                               final ElementData values,
	                                                               final int startPosition )
			throws MyUnsupportedQueryException
	{
		if ( elmts.get(startPosition) instanceof ElementData values2 ) {
			if ( startPosition + 1 == elmts.size() )
				return null;

			// Otherwise, merge that VALUES clause with the given VALUES clause
			// (by creating a cross-product of their respective sets of solution
			// mappings), and ...
			final ElementData mergedValues = merge(values, values2);

			// ... call this function recursively with the merged VALUES clause.
			return expandValuesPlusServicePattern(elmts, mergedValues, startPosition+1);
		}

		// Check that none of the variables bound by the given VALUES clause is
		// mentioned anywhere else except as the variable of a SERVICE clause.
		// i) collect all the variables mentioned anywhere else
		final PatternVarsVisitor varCollect = new PatternVarsVisitorWithoutServiceVars( new HashSet<>() );
		for ( int i = startPosition+1; i < elmts.size(); i++ ) {
			ElementWalker.walk( elmts.get(i), varCollect );
		}
		// ii) and check that none of them is bound by the given VALUES clause
		for ( final Var v : values.getVars() ) {
			if ( varCollect.acc.contains(v) )
				throw new MyUnsupportedQueryException("HeFQUIN does not support VALUES clauses with variables that are mentioned anywhere else than as the variable of a SERVICE clause (which is not the case for variable ?" + v.getVarName() + ").");
		}

		// Determine the position of the next VALUES clause in the given
		// list, starting from the given start position + 1 (+ 1 because
		// the element at the start position is not a VALUES clause;
		// otherwise, we would have ended up in the if-block above).
		final int posNextValuesClause = positionOfNextVALUES(elmts, startPosition+1);

		// Check that none of the elements in scope of the given VALUES clause
		// is, or contains, a SERVICE clause with a service variable that is not
		// one of the variables mentioned by the given VALUES clause.
		final VisitorToCheckServiceVariables vis = new VisitorToCheckServiceVariables( values.getVars() );
		for ( int i = startPosition+1; i < posNextValuesClause; i++ ) {
			ElementWalker.walk( elmts.get(i), vis );
			if ( vis.getDiscoveredVar() != null )
				throw new MyUnsupportedQueryException("HeFQUIN does not support SERVICE clauses with a variable that is not bound by the VALUES clause that comes closest before the SERVICE clause (which is not the case for variable ?" + vis.getDiscoveredVar().getVarName() + ").");
		}


		after that, call rewrite and then handle the rest
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

	protected static ElementData merge( final ElementData values1,
	                                    final ElementData values2 ) throws IllegalArgumentException {
		if ( ! Collections.disjoint(values1.getVars(), values2.getVars()) ) {
			// Throw exception, mentioning one such variable as an example.
			for ( final Var v : values1.getVars() ) {
				if ( values2.getVars().contains(v) )
					throw new IllegalArgumentException("Same variable(s) defined in different VALUES clauses (e.g., ?" + v.getVarName()+ ")");
			}
		}

		final List<Var> allVars = new ArrayList<>();
		allVars.addAll( values1.getVars() );
		allVars.addAll( values2.getVars() );

		final List<Binding> mergedSolMaps = new ArrayList<>();
		for ( final Binding b1 : values1.getRows() )
			for ( final Binding b2 : values1.getRows() )
				mergedSolMaps.add( BindingLib.merge(b1,b2) );

		return new ElementData(allVars, mergedSolMaps);
	}

	protected static Element rewrite( final ElementData valuesClause,
	                                  final List<Element> elmts,
	                                  final int startPosition,
	                                  final int endPosition ) {
		final Iterator<Binding> it = valuesClause.getRows().iterator();
		final Element rewriteUsingFirstRow = rewrite( it.next(), elmts, startPosition, endPosition );

		if ( ! it.hasNext() ) {
			return rewriteUsingFirstRow;
		}

		final ElementUnion eu = new ElementUnion();
		eu.addElement(rewriteUsingFirstRow);

		while ( it.hasNext() ) {
			final Element rewriteUsingNextRow = rewrite( it.next(), elmts, startPosition, endPosition );
			eu.addElement(rewriteUsingNextRow);
		}

		return eu;
	}

	protected static Element rewrite( final Binding solmap,
	                                  final List<Element> elmts,
	                                  final int startPosition,
	                                  final int endPosition ) {
		final ElementTransform transform = new MyElementTransform(solmap);
		final ElementGroup newElmts = new ElementGroup();
		for ( int i = startPosition; i <= endPosition; i++ ) {
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
				newElmts.addElement(eNew);
			}
		}

		// Add BIND clauses for all bindings in the given solution mapping.
		solmap.forEach( (var,node) -> {
			final NodeValue nv = NodeValue.makeNode(node);
			newElmts.addElement( new ElementBind(var, nv) );
		} );

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

	protected static class MyUnsupportedQueryException extends Exception {
		private static final long serialVersionUID = 8989008960497975684L;
		public MyUnsupportedQueryException( final String msg ) { super(msg); }
	}

	protected static class VisitorToCheckServiceVariables extends ElementVisitorBase {
		protected final List<Var> permittedVars;
		protected Var discoveredVar = null;

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

			if ( ! newServiceNode.isURI() )
				throw new IllegalArgumentException("VALUES clause can only assign IRIs to service variables. This is not the case for variable ?" + sn.getName() + ", which is assigned a " + sn.getClass().getName() + "(" + sn.toString(true)+ ").");

			return new ElementService( newServiceNode, inside, e.getSilent() );
		}
	}

}
