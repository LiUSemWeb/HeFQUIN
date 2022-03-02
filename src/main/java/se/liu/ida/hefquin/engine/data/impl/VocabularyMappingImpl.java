package se.liu.ida.hefquin.engine.data.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGroupPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;

public class VocabularyMappingImpl implements VocabularyMapping
{
	protected final Graph vocabularyMapping;
	
	public VocabularyMappingImpl() {
		vocabularyMapping = GraphFactory.createDefaultGraph();
	}
	
	// Source: https://jena.apache.org/documentation/io/rdf-input.html
	public VocabularyMappingImpl(final String rdfFile) {
		final Model mappingModel = RDFDataMgr.loadModel(rdfFile); //.nt file for N-Triple
		vocabularyMapping = mappingModel.getGraph();
	}
	
	public VocabularyMappingImpl(final Set<Triple> triples) {
		vocabularyMapping = GraphFactory.createDefaultGraph();
		for ( final Triple t : triples ) {
			vocabularyMapping.add(t);
		}
	}

	@Override
	public SPARQLGraphPattern translateTriplePattern(final TriplePattern tp) {
		final List<SPARQLGraphPattern> objectTranslation = new ArrayList<>();
		for(final TriplePattern i : translateSubject(tp)) {
			objectTranslation.add(translateObject(i));
		}
		
		final List<SPARQLGraphPattern> predicateTranslation = new ArrayList<>();
		for(final SPARQLGraphPattern j : objectTranslation) {
			
			if(j instanceof SPARQLUnionPattern) {
				
				final SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl();
				for(final SPARQLGraphPattern k: ((SPARQLUnionPattern) j).getSubPatterns()) {		
					if(k instanceof TriplePattern) {
						union.addSubPattern(translatePredicate((TriplePattern) k));
					} else if (k instanceof BGP) {
						for(final TriplePattern l : ((BGP) k).getTriplePatterns()) {
// TODO: I think that this part here is incorrect, please double check
							union.addSubPattern(translatePredicate(l));
						}
					} else {
						throw new IllegalArgumentException(k.getClass().getName());
					}
				}
				predicateTranslation.add(union);
				
			} else if (j instanceof BGP) { 
				// try to create a BGP if possible (which is the case if all
				// the graph patterns resulting from the predicate translation
				// are triple patterns); if not possible, then create a group
				// graph pattern
				final List<SPARQLGraphPattern> allSubPatterns = new ArrayList<>();
				final Set<TriplePattern> tpSubPatterns = new HashSet<>();
				boolean allSubPatternsAreTriplePatterns = true; // assume yes

				for( final TriplePattern m : ((BGP) j).getTriplePatterns() ) {
					final SPARQLGraphPattern p = translatePredicate(m);
					allSubPatterns.add(p);

					if ( allSubPatternsAreTriplePatterns && p instanceof TriplePattern ) {
						tpSubPatterns.add( (TriplePattern) p );
					}
					else {
						allSubPatternsAreTriplePatterns = false;
					}
				}

				if ( allSubPatternsAreTriplePatterns ) {
					predicateTranslation.add( new BGPImpl(tpSubPatterns) );
				}
				else {
					predicateTranslation.add( new SPARQLGroupPatternImpl(allSubPatterns) );
				}

			} else if (j instanceof TriplePattern) {
				predicateTranslation.add(translatePredicate((TriplePattern) j));
				
			} else {
				throw new IllegalArgumentException(j.getClass().getName());
			}
		}
		
		if(predicateTranslation.size() > 1) {
			return new SPARQLUnionPatternImpl(predicateTranslation);
		} else {
			return predicateTranslation.get(0);
		}
	}
	
	protected Set<TriplePattern> translateSubject(final TriplePattern tp){
		final Set<TriplePattern> results = new HashSet<>();
		final Triple jenaTP = tp.asJenaTriple();
		if (jenaTP.getSubject().isVariable()) {
			results.add(tp);
			return results;
		}

		final Set<Triple> mappings = getMappings( jenaTP.getSubject(), Node.ANY, Node.ANY );
		for (final Triple m : mappings) {
			if ( m.getPredicate().equals(OWL.sameAs.asNode()) ) {
				final TriplePattern translation = new TriplePatternImpl(m.getObject(), jenaTP.getPredicate(), jenaTP.getObject());
				results.add(translation);
			} else {
				throw new IllegalArgumentException(m.getPredicate().getURI());
			}
		}
		return results;
	}
	
	protected SPARQLGraphPattern translateObject(final TriplePattern tp){
		final Triple jenaTP = tp.asJenaTriple();
		if(jenaTP.getObject().isVariable()) {
			return tp;
		}

		final List<SPARQLGraphPattern> resultsList = new ArrayList<>();

		final Set<Triple> mappings = getMappings( jenaTP.getObject(), Node.ANY, Node.ANY );
		for (final Triple m : mappings) {
			final Node predicate = m.getPredicate();
			
			if(predicate.equals(OWL.sameAs.asNode())) {
				final TriplePattern translation = new TriplePatternImpl(jenaTP.getSubject(), jenaTP.getPredicate(), m.getObject());
				resultsList.add(translation);
			} else {
				if (!jenaTP.getPredicate().isURI()) {
					resultsList.add(tp);
					continue;
				} else {
					if(!jenaTP.getPredicate().equals(RDF.type.asNode())) {
						resultsList.add(tp);
						continue;
					}
				}
				if (predicate.equals(OWL.equivalentClass.asNode())) {
					
					/*
					 * Blank node used to help create intersections and unions
					 * (a equals b)
					 * (b unionof c)
					 * (b unionof d)
					 */
					if (m.getObject().isBlank()) {
						//Union or intersection
						Node newPredicate = null;
						final List<Node> objects = new ArrayList<>();
						final Set<Triple> subMappings = getMappings( m.getObject(), Node.ANY, Node.ANY );
						for(final Triple i : subMappings) {
							final Node iPredicate = i.getPredicate();
							if(newPredicate == null) {
								newPredicate = iPredicate;
							} else if (!iPredicate.equals(newPredicate)) {
								throw new IllegalArgumentException( iPredicate.toString() );		
							}
							objects.add(i.getObject());
						}
						
						if (newPredicate.equals(OWL.unionOf.asNode())) {
							final SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl();
							for(final Node j : objects){
								final TriplePattern translation = new TriplePatternImpl(jenaTP.getSubject(), jenaTP.getPredicate(), j);
								union.addSubPattern(translation);
							}
							resultsList.add(union);
							
						} else if (newPredicate.equals(OWL.intersectionOf.asNode())) {
							 final BGPImpl intersection = new BGPImpl();
							 for( final Node j : objects ){
							  	final TriplePattern translation = new TriplePatternImpl(jenaTP.getSubject(), jenaTP.getPredicate(), j);
							  	intersection.addTriplePattern(translation);
							 }
							 resultsList.add(intersection); 
							 
						} else {
							throw new IllegalArgumentException( newPredicate.toString() );
						}		
						
					} else {
						final TriplePattern translation = new TriplePatternImpl(jenaTP.getSubject(), jenaTP.getPredicate(), m.getObject());
						resultsList.add(translation);	
					}
				} else {
					throw new IllegalArgumentException( predicate.toString() );
				}
			}
		}
		
		if(resultsList.size() == 0) {
			return tp;
		} else if (resultsList.size() > 1) {
			return new SPARQLUnionPatternImpl(resultsList);
		} else {
			return resultsList.get(0);
		}
	}

	protected SPARQLGraphPattern translatePredicate(final TriplePattern tp){
		final Triple jenaTP = tp.asJenaTriple();
		if(jenaTP.getPredicate().isVariable()) {
			return tp;
		}

		final List<SPARQLGraphPattern> resultsList = new ArrayList<SPARQLGraphPattern>();

		final Set<Triple> mappings = getMappings( jenaTP.getPredicate(), Node.ANY, Node.ANY );
		for (final Triple m : mappings) {
			final Node predicate = m.getPredicate();
			
			if (predicate.equals(RDFS.subPropertyOf.asNode())) {
				final TriplePattern translation = new TriplePatternImpl(jenaTP.getSubject(), m.getObject(), jenaTP.getObject());
				resultsList.add(translation);
				
			} else if (predicate.equals(OWL.inverseOf.asNode())){
				final TriplePattern translation = new TriplePatternImpl(jenaTP.getObject(), m.getObject(), jenaTP.getSubject());
				resultsList.add(translation);	
				
			} else if (predicate.equals(OWL.equivalentProperty.asNode())){
				if (m.getObject().isBlank()) {
					//Union or intersection
					Node newPredicate = null;
					final List<Node> objects = new ArrayList<>();

					final Set<Triple> subMappings = getMappings( m.getObject(), Node.ANY, Node.ANY );
					for(final Triple i : subMappings) {
						final Node iPredicate = i.getPredicate();
						if(newPredicate == null) {
							newPredicate = iPredicate;
						} else {
							if (!iPredicate.equals(newPredicate)) {
								throw new IllegalArgumentException( iPredicate.toString() );
							}
						}
						objects.add(i.getObject());
					}
					
					if (newPredicate.equals(OWL.unionOf.asNode())) {
						final SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl();
						for ( final Node j : objects ) {
							final TriplePattern translation = new TriplePatternImpl(jenaTP.getSubject(), j, jenaTP.getObject());
							union.addSubPattern(translation);
						}
						resultsList.add(union);
						
					} else if (newPredicate.equals(OWL.intersectionOf.asNode())) {
						 final BGPImpl intersection = new BGPImpl();
						 for ( final Node j : objects ) {
						  	final TriplePattern translation = new TriplePatternImpl(jenaTP.getSubject(), j,  jenaTP.getObject());
						  	intersection.addTriplePattern(translation);
						 }
						 resultsList.add(intersection); 
						 
					} else {
						throw new IllegalArgumentException( newPredicate.toString() );
					}
					
				} else {
					final TriplePattern translation = new TriplePatternImpl(jenaTP.getSubject(), m.getObject(), jenaTP.getObject());
					resultsList.add(translation);	
				}
				
			} else {
				throw new IllegalArgumentException( predicate.toString() );
			}
		}
		
		if(resultsList.size() == 0) {
			return tp;
		} else if (resultsList.size() > 1) {
			return new SPARQLUnionPatternImpl(resultsList);
		} else {
			return resultsList.get(0);
		}
	}
	
	protected Set<Triple> getMappings( final Node s, final Node p, final Node o ){
		final Set<Triple> mappings = new HashSet<>();
		final Iterator<Triple> i = vocabularyMapping.find(s,p,o);
		while(i.hasNext()) {
			mappings.add(i.next());
		}
		return mappings;
	}

	public Graph getVocabularyMappingAsGraph() {
		return vocabularyMapping;
	}

}
