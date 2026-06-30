package se.liu.ida.hefquin.base.data.mappings.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.data.mappings.EntityMapping;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;

import org.apache.jena.graph.Triple;

public class EntityMappingImpl implements EntityMapping
{
	protected final Map<Node, Set<Node>> g2lMap;
	protected final Map<Node, Set<Node>> l2gMap;

	public EntityMappingImpl( final Map<Node, Set<Node>> g2l ) {
		g2lMap = new HashMap<>(g2l);
		l2gMap = createL2G(g2l);
	}

	protected static Map<Node, Set<Node>> createL2G( final Map<Node, Set<Node>> g2lMap ) {
		final Map<Node, Set<Node>> l2gMap = new HashMap<>();

		for ( final Map.Entry<Node, Set<Node>> e : g2lMap.entrySet() ) {
			final Node g = e.getKey();

			for ( final Node l : e.getValue() ) {
				// Record in l2gMap that g is a global term for the local
				// term l. To this end, first check whether we already have
				// other global terms for l.
				final Set<Node> globalTermsForL = l2gMap.get(l);
				if ( globalTermsForL != null ) {
					// There is already at least one other global term for l.
					// We can simply add g to the set.
					globalTermsForL.add(g);
				}
				else {
					// We have not seen l before, which means we need to
					// create a new entry in l2gMap for it and, then, add
					// g into the value set of this new entry.
					final Set<Node> newGlobalTermsForL = new HashSet<>();
					l2gMap.put(l, newGlobalTermsForL);
					newGlobalTermsForL.add(g);
				}
			}
		}

		return l2gMap;
	}

	@Override
	public Set<TriplePattern> applyToTriplePattern( final TriplePattern tp ) {
		final Node s = tp.asJenaTriple().getSubject();
		final Node p = tp.asJenaTriple().getPredicate();
		final Node o = tp.asJenaTriple().getObject();

		final Set<Triple> workingSet;

		if ( s.isURI() ) {
			final Set<Node> localSubjects = g2lMap.get(s);
			if( localSubjects != null ) {
				workingSet = new HashSet<Triple>();
				for ( final Node localSubject : localSubjects ) {
					final Triple t = Triple.create(localSubject, p, o);
					workingSet.add(t);
				}
			}
			else {
				workingSet = null;
			}
		}
		else {
			workingSet = null;
		}

		final Set<TriplePattern> resultTPs = new HashSet<>();
		if( o.isURI() ) {
			final Set<Node> localObjects = g2lMap.get(o);
			if( localObjects != null ) {
				final Set<Triple> workingSet2;
				if ( workingSet != null ) {
					workingSet2 = workingSet;
				}
				else {
					workingSet2 = Collections.singleton( tp.asJenaTriple() );
				}

				for ( final Triple t : workingSet2 ) {
					for ( final Node localObject : localObjects ) {
						final Triple tt = Triple.create( t.getSubject(), t.getPredicate(), localObject );
						resultTPs.add( new TriplePatternImpl(tt) );
					}
				}
				return resultTPs;
			}
		}

		if ( workingSet == null ) {
			resultTPs.add(tp);
		}
		else {
			for ( final Triple t : workingSet ) {
				resultTPs.add( new TriplePatternImpl(t) );
			}
		}

		return resultTPs;
	}

	@Override
	public Set<SolutionMapping> applyToSolutionMapping( final SolutionMapping sm ) {
		final Set<SolutionMapping> result = applyMapToSolutionMapping(sm, g2lMap);
		return result;
	}

	@Override
	public Set<SolutionMapping> applyInverseToSolutionMapping( final SolutionMapping sm ) {
		final Set<SolutionMapping> result = applyMapToSolutionMapping(sm, l2gMap);
		return result;
	}

	static public Set<SolutionMapping> applyMapToSolutionMapping( final SolutionMapping solMap,
	                                                              final Map<Node, Set<Node>> x2yMap ) {
		final Binding sm = solMap.asJenaBinding();

		// If the given solution mapping is the empty mapping, simply return it unchanged.
		if ( sm.isEmpty() ) {
			return Collections.singleton(solMap);
		}

		Set<BindingBuilder> builders = new HashSet<>();
		builders.add( BindingBuilder.create() );

		final Iterator<Var> it = sm.vars();
		while ( it.hasNext() ) {
			final Var var = it.next();
			final Node node = sm.get(var);

			if ( ! node.isURI() ) {
				// If the RDF term that the given solution mapping binds to
				// the current variable is not a URI, simply add the current
				// var-term pair to the binding builders.
				for( final BindingBuilder bb : builders ) {
					bb.add(var, node);
				}
			}
			else {
				// If the RDF term that the given solution mapping binds to
				// the current variable is a URI, check whether this URI is
				// mapped by this entity mapping.
				final Set<Node> mappedNodes = x2yMap.get(node);
				if ( mappedNodes == null || mappedNodes.isEmpty() ) {
					// If the URI is not mapped by this entity mapping, simply
					// add the current var-term pair to the binding builders.
					for( final BindingBuilder bb : builders ) {
						bb.add(var, node);
					}
				}
				else {
					// The URI is mapped by this entity mapping.
					final Set<BindingBuilder> newBuilders = new HashSet<>();
					for( final BindingBuilder bb : builders ) {
						final Binding b = bb.build();
						for ( final Node mappedNode : mappedNodes ) {
							final BindingBuilder newBB = BindingBuilder.create(b);
							newBB.add(var, mappedNode);
							newBuilders.add(newBB);
						}
					}

					builders = newBuilders;
				}
			}
		}

		final Set<SolutionMapping> result = new HashSet<>();
		for( final BindingBuilder bb : builders ) {
			final SolutionMapping newSolMap = new SolutionMappingImpl( bb.build() );
			result.add(newSolMap);
		}

		return result;
	}

	@Override
	public Expr applyToExpression( final Expr expr ) {
		if ( expr instanceof E_LogicalAnd and ) {
			final Expr l = applyToExpression( and.getArg1() );
			final Expr r = applyToExpression( and.getArg2() );

			if ( l == null || r == null )
				throw new UnsupportedOperationException( "Filter expression " + expr + " cannot be rewritten" );

			if ( and.getArg1().equals(l) && and.getArg2().equals(r) )
				return expr;

			return new E_LogicalAnd( l, r );
		}

		if ( expr instanceof E_LogicalOr or ) {
			final Expr l = applyToExpression( or.getArg1()) ;
			final Expr r = applyToExpression( or.getArg2()) ;

			if ( l == null || r == null )
				throw new UnsupportedOperationException( "Filter expression " + expr + " cannot be rewritten" );

			if ( or.getArg1().equals(l) && or.getArg2().equals(r) )
				return expr;

			return new E_LogicalOr( l, r );
		}

		if ( expr instanceof E_Equals || expr instanceof E_NotEquals ) {
			return rewriteComparison( expr );
		}

		// other rexpression types aren't considered rewritable at the moment
		throw new UnsupportedOperationException( "Filter expression " + expr + " cannot be rewritten" );
	}

	private Expr rewriteComparison( final Expr expr ) {
		final Expr left;
		final Expr right;
		final boolean equals;

		if ( expr instanceof E_Equals eq ) {
			left = eq.getArg1();
			right = eq.getArg2();
			equals = true;
		}
		else if ( expr instanceof E_NotEquals neq ) {
			left = neq.getArg1();
			right = neq.getArg2();
			equals = false;
		}
		else
			throw new UnsupportedOperationException( "Filter expression " + expr + " cannot be rewritten" );

		final Node node;
		final boolean rewriteLeft;
		if ( left.isConstant() ) {
			rewriteLeft = true;
			node = ( (NodeValue) left ).asNode();
		}
		else if ( right.isConstant() ) {
			rewriteLeft = false;
			node = ( (NodeValue) right ).asNode();
		}
		else
			throw new UnsupportedOperationException( "Filter expression " + expr + " cannot be rewritten" );

		if ( ! node.isURI() )
			throw new UnsupportedOperationException( "Filter expression " + expr + " cannot be rewritten" );

		final Set<Node> nodes = mapGlobalTermToLocalTerms(node);

		if ( nodes.size() == 1 ) {
			final Expr translated = NodeValue.makeNode(nodes.iterator().next());
			if ( equals )
				return rewriteLeft ? new E_Equals(translated, right) : new E_Equals(left, translated);
			else
				return rewriteLeft ? new E_NotEquals(translated, right) : new E_NotEquals(left, translated);
		}

		final List<Expr> rewritten = new ArrayList<>();

		for ( final Node n : nodes ) {
			final NodeValue translated = NodeValue.makeNode(n);
			if ( equals )
				rewritten.add( rewriteLeft ? new E_Equals(translated, right) : new E_Equals(left, translated) );
			else
				rewritten.add( rewriteLeft ? new E_NotEquals(translated, right) : new E_NotEquals(left, translated) );
		}

		return equals ? buildOr(rewritten) : buildAnd(rewritten);
	}

	private static Expr buildOr( final List<Expr> exprList ) {
		if ( exprList == null || exprList.isEmpty() ) {
			throw new IllegalArgumentException( "Empty OR list" );
		}

		Expr result = exprList.get(0);

		for ( int i = 1; i < exprList.size(); i++ ) {
			result = new E_LogicalOr( result, exprList.get(i) );
		}

		return result;
	}

	private static Expr buildAnd( final List<Expr> exprList ) {
		if ( exprList == null || exprList.isEmpty() ) {
			throw new IllegalArgumentException("Empty AND list");
		}

		Expr result = exprList.get(0);

		for ( int i = 1; i < exprList.size(); i++ ) {
			result = new E_LogicalAnd( result, exprList.get(i) );
		}

		return result;
	}

	private Set<Node> mapGlobalTermToLocalTerms( final Node n ) {
		if ( ! n.isURI() ) {
			return Collections.singleton(n);
		}

		final Set<Node> mappings = g2lMap.get(n);

		if ( mappings == null || mappings.isEmpty() )
			return Collections.singleton(n);

		return mappings;
	}
}
