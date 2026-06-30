package se.liu.ida.hefquin.base.data.mappings.impl;

import java.util.*;

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
import org.apache.jena.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.data.mappings.SchemaMapping;
import se.liu.ida.hefquin.base.data.mappings.TermMapping;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;

public class SchemaMappingImpl implements SchemaMapping
{
	private static final Logger log = LoggerFactory.getLogger( SchemaMappingImpl.class );
	/**
	 * The keys of this map are global terms, and the value for each such
	 * global term is a set of all term mappings that have the global term
	 * as their {@link TermMapping#getGlobalTerm()}.
	 * The union of all the sets that are values in this map is exactly the
	 * same set of term mappings as the union of all the sets that are values
	 * in {@link #l2gMap}.
	 */
	protected final Map<Node, Set<TermMapping>> g2lMap;

	/**
	 * The keys of this map are local terms, and the value for each such
	 * local term is a set of all term mappings that have the local term
	 * as an element of their {@link TermMapping#getLocalTerms()}.
	 * The union of all the sets that are values in this map is exactly the
	 * same set of term mappings as the union of all the sets that are values
	 * in {@link #g2lMap}.
	 */
	protected final Map<Node, Set<TermMapping>> l2gMap;

	protected final boolean isEquivalenceOnly;

	public SchemaMappingImpl( final Map<Node, Set<TermMapping>> g2lMap ) {
		this.g2lMap = g2lMap;
		this.l2gMap = new HashMap<>();
		this.isEquivalenceOnly = populateL2G(g2lMap, l2gMap);
	}

	/**
	 * Populates the given l2gMap based on the given g2lMap and returns whether
	 * all the {@link TermMapping}s in g2lMap are equivalence mappings only.
	 */
	protected static boolean populateL2G( final Map<Node, Set<TermMapping>> g2lMap,
	                                      final Map<Node, Set<TermMapping>> l2gMap ) {
		// begin by assuming that all term mappings are equivalence mappings only
		boolean isEquivalenceOnly = true;

		for ( final Map.Entry<Node, Set<TermMapping>> e : g2lMap.entrySet() ) {
			final Node globalTerm = e.getKey();
			for ( final TermMapping tm : e.getValue() ) {
				assert tm.getGlobalTerm() == globalTerm;

				if (    ! tm.getTypeOfRule().equals(OWL.equivalentClass.asNode())
				     && ! tm.getTypeOfRule().equals(OWL.equivalentProperty.asNode()) ) {
					isEquivalenceOnly = false;
				}

				for ( final Node localTerm : tm.getLocalTerms() ) {
					Set<TermMapping> l2gEntry = l2gMap.get(localTerm);

					if ( l2gEntry == null ) {
						l2gEntry = new HashSet<>();
						l2gMap.put(localTerm, l2gEntry);
					}

					l2gEntry.add(tm);
				}
			}
		}

		return isEquivalenceOnly;
	}

	@Override
	public boolean isEquivalenceOnly() {
		return isEquivalenceOnly;
	}

	@Override
	public SPARQLGraphPattern applyToTriplePattern( final TriplePattern tp ) {
		final Node givenSubject   = tp.asJenaTriple().getSubject();
		final Node givenPredicate = tp.asJenaTriple().getPredicate();
		final Node givenObject    = tp.asJenaTriple().getObject();

		// Collect all ways of translating the object of the given triple pattern
		final Set<Node> newObjects = new HashSet<>();
		if( givenObject.isURI() ) {
			final Set<TermMapping> mappingsForObject = g2lMap.get(givenObject);
			if( mappingsForObject != null && ! mappingsForObject.isEmpty() ) {
				for ( final TermMapping tm : mappingsForObject ) {
					for ( final Node localTerm : tm.getLocalTerms() ) {
						newObjects.add(localTerm);
					}
				}
			}
		}

		final boolean keepGivenObject = newObjects.isEmpty();
		if ( keepGivenObject ) {
			newObjects.add(givenObject);
		}

		// Collect all ways of translating the predicate of the given triple pattern
		final Set<Node> newPredicates = new HashSet<>();
		if( givenPredicate.isURI() ) {
			log.debug("Given predicate: {}", givenPredicate);
			log.debug("Available mapping keys: {}", g2lMap.keySet());
			final Set<TermMapping> mappingsForPredicate = g2lMap.get(givenPredicate);
			if( mappingsForPredicate != null && ! mappingsForPredicate.isEmpty() ) {
				for ( final TermMapping tm : mappingsForPredicate ) {
					for ( final Node localTerm : tm.getLocalTerms() ) {
						newPredicates.add(localTerm);
					}
				}
			}
		}

		final boolean keepGivenPredicate = newPredicates.isEmpty();
		if ( keepGivenPredicate ) {
			newPredicates.add(givenPredicate);
		}

		if ( keepGivenPredicate && keepGivenObject ) {
			return tp;
		}

		if ( newPredicates.size() == 1 && newObjects.size() == 1 ) {
			return new TriplePatternImpl( givenSubject,
			                              newPredicates.iterator().next(),
			                              newObjects.iterator().next() );
		}

		// Construct SPARQLUnionPattern
		final SPARQLUnionPatternImpl newPattern = new SPARQLUnionPatternImpl();
		for ( final Node newP : newPredicates ){
			for ( final Node newO : newObjects ){
				final TriplePattern newTP = new TriplePatternImpl(givenSubject, newP, newO);
				newPattern.addSubPattern(newTP);
			}
		}

		return newPattern;
	}

	@Override
	public Set<SolutionMapping> applyToSolutionMapping( final SolutionMapping sm ) {
		return applyToSolutionMapping(sm, false);
	}

	@Override
	public Set<SolutionMapping> applyInverseToSolutionMapping( final SolutionMapping sm ) {
		return applyToSolutionMapping(sm, true);
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

		if ( ! right.isConstant() )
			throw new UnsupportedOperationException( "Filter expression " + expr + " cannot be rewritten" );

		final Node node = ( (NodeValue) right ).asNode();
		if ( ! node.isURI() )
			throw new UnsupportedOperationException( "Filter expression " + expr + " cannot be rewritten" );

		final Set<Node> nodes = mapGlobalTermToLocalTerms(node);

		if ( nodes.isEmpty() )
			throw new UnsupportedOperationException( "Filter expression " + expr + " cannot be rewritten" );

		if ( nodes.size() == 1 ) {
			if ( equals )
				return new E_Equals(left, NodeValue.makeNode(nodes.iterator().next()));
			else
				return new E_NotEquals(left, NodeValue.makeNode(nodes.iterator().next()));
		}

		final List<Expr> rewritten = new ArrayList<>();

		for ( final Node n : nodes ) {
			if ( equals )
				rewritten.add( new E_Equals(left, NodeValue.makeNode(n)) );
			else
				rewritten.add( new E_NotEquals(left, NodeValue.makeNode(n)) );
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

	protected Set<SolutionMapping> applyToSolutionMapping( final SolutionMapping solMap,
	                                                       final boolean useInverse ) {
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
				final Set<TermMapping> mappingsForTerm = useInverse ? l2gMap.get(node) : g2lMap.get(node);

				if ( mappingsForTerm == null || mappingsForTerm.isEmpty() ) {
					// If the URI is not mapped by this schema mapping, simply
					// add the current var-term pair to the binding builders.
					for( final BindingBuilder bb : builders ) {
						bb.add(var, node);
					}
				}
				else {
					// The URI is mapped by this schema mapping.
					final Set<Node> termsForTerm;
					if ( useInverse )
						termsForTerm = extractGlobalTermsForLocalTerm(mappingsForTerm);
					else
						termsForTerm = extractLocalTermsForGlobalTerm(mappingsForTerm);

					final Set<BindingBuilder> newBuilders = new HashSet<>();
					for( final BindingBuilder bb : builders ) {
						final Binding b = bb.build();
						for ( final Node newTerm : termsForTerm ) {
							final BindingBuilder newBB = BindingBuilder.create(b);
							newBB.add(var, newTerm);
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

	private Set<Node> mapGlobalTermToLocalTerms( final Node n ) {
		final Set<Node> result = new HashSet<>();

		if ( n.isURI() ) {
			final Set<TermMapping> mappings = g2lMap.get(n);

			if ( mappings != null && ! mappings.isEmpty() ) {
				for ( final TermMapping tm : mappings ) {
					result.addAll( tm.getLocalTerms() );
				}
			}
		}

		if ( result.isEmpty() ) {
			result.add(n);
		}

		return result;
	}

	protected Set<Node> extractGlobalTermsForLocalTerm( final Set<TermMapping> mappingsForTerm ) {
		final Set<Node> result = new HashSet<>();
		for ( final TermMapping tm : mappingsForTerm ) {
			result.add( tm.getGlobalTerm() );
		}
		return result;
	}

	protected Set<Node> extractLocalTermsForGlobalTerm( final Set<TermMapping> mappingsForTerm ) {
		// only use equivalence rules
		final Set<Node> result = new HashSet<>();
		for ( final TermMapping tm : mappingsForTerm ) {
			final Node ruleType = tm.getTypeOfRule();
			if (    OWL.equivalentClass.asNode().equals(ruleType)
			     || OWL.equivalentProperty.asNode().equals(ruleType) ) {
				result.addAll( tm.getLocalTerms() );
			}
		}

		return result;
	}

}
