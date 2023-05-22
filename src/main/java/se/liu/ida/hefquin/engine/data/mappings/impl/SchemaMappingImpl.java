package se.liu.ida.hefquin.engine.data.mappings.impl;

import java.util.*;

import org.apache.jena.graph.Graph;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.data.mappings.SchemaMapping;
import se.liu.ida.hefquin.engine.data.mappings.TermMapping;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.utils.Pair;

public class SchemaMappingImpl implements SchemaMapping
{
	protected final Graph mappingDescription;
	protected final Map<Node, Set<TermMapping>> g2lMap = new HashMap<>();
	protected final Map<Node, Set<TermMapping>> l2gMap = new HashMap<>();
	boolean isEquivalenceOnly = true;

	public SchemaMappingImpl( final Graph mappingDescription ) {
		this.mappingDescription = mappingDescription;
		parseMappingDescription(mappingDescription);
	}

	protected boolean parseMappingDescription( final Graph mappingDescription ) {
		// Populate both g2lMap and l2gMap based on the type of TermMapping statements in the given RDF graph
		final Iterator<Triple> it = mappingDescription.find(Node.ANY, Node.ANY, Node.ANY);
		while ( it.hasNext() ) {
			final Triple workingTriple = it.next();

			if ( workingTriple.getPredicate().equals(OWL.equivalentClass.asNode())
				|| workingTriple.getPredicate().equals(OWL.equivalentProperty.asNode()) ) {
				populate( Collections.singleton(workingTriple.getObject()), workingTriple.getSubject(), workingTriple.getPredicate() );
			}
			else if ( workingTriple.getPredicate().equals(RDFS.subClassOf.asNode())
				|| workingTriple.getPredicate().equals(RDFS.subPropertyOf.asNode()) ) {
				populate( Collections.singleton(workingTriple.getSubject()), workingTriple.getObject(), workingTriple.getPredicate() );
				isEquivalenceOnly = false;
			}
			else if ( workingTriple.getPredicate().equals(OWL.unionOf.asNode())) {
				isEquivalenceOnly = false;
				final Set<Node> localTerms = new HashSet<>();

				// Get all local terms
				Pair<Node, Node> linkMapping = getConnectedMapping( workingTriple.getObject() );
				while(true) {
					localTerms.add(linkMapping.object1);
					if (linkMapping.object2.equals(RDF.nil.asNode())) {
						break;
					}
					linkMapping = getConnectedMapping(linkMapping.object2);
				}

				populate( localTerms, workingTriple.getSubject(), OWL.unionOf.asNode() );
			}
		}
		return isEquivalenceOnly;
	}

	protected void populate( final Set<Node> local, final Node global, final Node mappingType ){
		// Populate g2lMap
		Set<TermMapping> g2ltermMappings = g2lMap.get( global );

		if ( g2ltermMappings == null ) {
			g2ltermMappings = new HashSet<>();
			g2lMap.put(global, g2ltermMappings);
		}

		final TermMappingImpl localTerms = new TermMappingImpl(mappingType);
		localTerms.addTranslatedTerm(local);
		g2ltermMappings.add(localTerms);

		// Populate l2gMap
		for ( final Node localTerm: local ) {
			Set<TermMapping> l2gtermMappings = l2gMap.get(localTerm);

			if (l2gtermMappings == null) {
				l2gtermMappings = new HashSet<>();
				l2gMap.put(localTerm, l2gtermMappings);
			}

			TermMappingImpl globalTerms = new TermMappingImpl(mappingType);
			globalTerms.addTranslatedTerm(global);
			l2gtermMappings.add(globalTerms);
		}

	}

	@Override
	public SPARQLGraphPattern applyToTriplePattern( final TriplePattern tp ) {
		// Translate the object of the given triple pattern
		final Set<Node> localObjects = new HashSet<>();
		if( tp.asJenaTriple().getObject().isURI() ) {
			final Set<TermMapping> g2lObjectMappings = g2lMap.get( tp.asJenaTriple().getObject() );
			if( g2lObjectMappings != null ) {
				for (final TermMapping mappingType : g2lObjectMappings) {
					localObjects.addAll( mappingType.getTranslatedTerms() );
				}
			}
			else {
				localObjects.add( tp.asJenaTriple().getObject() );
			}
		}
		else {
			localObjects.add( tp.asJenaTriple().getObject() );
		}

		// Translate the predicate of the given triple pattern
		final Set<Node> localPredicate = new HashSet<>();
		if( tp.asJenaTriple().getPredicate().isURI() ) {
			final Set<TermMapping> g2lSubjectMappings = g2lMap.get( tp.asJenaTriple().getPredicate() );
			if( g2lSubjectMappings != null ) {
				for ( final TermMapping mappingType: g2lSubjectMappings ) {
					localPredicate.addAll( mappingType.getTranslatedTerms() );
				}
			}
			else {
				localPredicate.add( tp.asJenaTriple().getPredicate() );
			}
		}
		else {
			localPredicate.add( tp.asJenaTriple().getPredicate() );
		}

		// Construct SPARQLUnionPattern
		final SPARQLUnionPatternImpl graph = new SPARQLUnionPatternImpl();
		for ( Node predicate : localPredicate ){
			for ( Node object: localObjects ){
				final TriplePattern resultTriple = new TriplePatternImpl( tp.asJenaTriple().getSubject(), predicate, object );
				graph.addSubPattern( resultTriple );
			}
		}

		return graph;
	}

	@Override
	/**
	 * Translate solution mappings that using local vocabulary to global vocabularies
	 */
	public Set<SolutionMapping> applyToSolutionMapping( final SolutionMapping sm ) {
		return applyMapToSolutionMapping(sm, false);
	}

	@Override
	public Set<SolutionMapping> applyInverseToSolutionMapping( final SolutionMapping sm ) {
		return applyMapToSolutionMapping(sm, true);
	}

	protected Set<SolutionMapping> applyMapToSolutionMapping(final SolutionMapping sm, final boolean useInverse ) {
		final Binding binding = sm.asJenaBinding();
		// If the given solution mapping is the empty mapping, simply return it unchanged.
		if ( binding.isEmpty() ) {
			return Collections.singleton(sm);
		}

		final Set<Binding> resultsBindings = new HashSet<>();
		final Set<Binding> tmpBindings = new HashSet<>();

		resultsBindings.add( BindingBuilder.create().build() );
		final Iterator<Var> it = binding.vars();
		while ( it.hasNext() ) {
			final Var v = it.next();
			final Set<Node> terms;
			if ( useInverse ) {
				terms = applyMapToGlobalURI( binding.get(v) );
			}
			else {
				terms = applyMapToLocalURI( binding.get(v) );
			}

			for ( final Node uri: terms ) {
				for ( Binding b : resultsBindings ){
					final BindingBuilder newBinding = BindingBuilder.create(b);
					newBinding.add(v, uri);
					tmpBindings.add( newBinding.build() );
				}
			}
			resultsBindings.clear();
			resultsBindings.addAll( tmpBindings );
			tmpBindings.clear();
		}

		final Set<SolutionMapping> resultsSolMaps = new HashSet<>();
		for ( Binding b: resultsBindings ){
			resultsSolMaps.add( new SolutionMappingImpl(b) );
		}
		return resultsSolMaps;
	}

	public Set<Node> applyMapToLocalURI( final Node node ) {
		final Set<TermMapping> termMappings = l2gMap.get(node);
		final Set<Node> results = new HashSet<>();
		if ( termMappings != null ) {
			for ( final TermMapping mappingTypes : termMappings ) {
				results.addAll( mappingTypes.getTranslatedTerms() );
			}
		}
		else {
			results.add(node);
		}

		return results;
	}

	/**
	 * Only apply equivalent rules
	 */
	public Set<Node> applyMapToGlobalURI( final Node node ) {
		final Set<TermMapping> termMappings = g2lMap.get(node);
		final Set<Node> results = new HashSet<>();
		if ( termMappings != null ) {
			for ( final TermMapping mappingTypes : termMappings ) {
				if ( mappingTypes.getTypeOfRule().equals(OWL.equivalentClass.asNode()) || mappingTypes.getTypeOfRule().equals(OWL.equivalentProperty.asNode()) ) {
					results.addAll(mappingTypes.getTranslatedTerms());
				}
			}
		}
		else {
			results.add(node);
		}

		return results;
	}

	public boolean isEquivalenceOnly() {
		return isEquivalenceOnly;
	}

	protected Pair<Node, Node> getConnectedMapping( final Node head ){
		Node first = RDF.nil.asNode();
		Node rest = RDF.nil.asNode();
		boolean hasFirst = false;
		boolean hasRest = false;
		final Iterator<Triple> i = mappingDescription.find(head, Node.ANY, Node.ANY);
		while(i.hasNext()) {
			final Triple next = i.next();
			if ( next.getPredicate().equals(RDF.first.asNode()) ) {
				if ( !hasFirst ) {
					hasFirst = true;
					first = next.getObject();
				} else {
					throw new IllegalArgumentException(next.getPredicate().toString());
				}
			} else if ( next.getPredicate().equals(RDF.rest.asNode()) ) {
				if (!hasRest) {
					hasRest = true;
					rest = next.getObject();
				} else {
					throw new IllegalArgumentException(next.getPredicate().toString());
				}
			} else {
				throw new IllegalArgumentException(next.getPredicate().toString());
			}
		}
		if ( hasFirst && hasRest ) {
			final Pair<Node, Node> mapping = new Pair(first, rest);
			return mapping;
		} else {
			throw new IllegalArgumentException(hasFirst + ", " + hasRest);
		}
	}

}
