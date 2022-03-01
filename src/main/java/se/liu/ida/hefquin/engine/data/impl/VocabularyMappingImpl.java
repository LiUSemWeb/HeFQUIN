package se.liu.ida.hefquin.engine.data.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGroupPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;

public class VocabularyMappingImpl implements VocabularyMapping{
	
	protected final Graph vocabularyMapping;
	
	public VocabularyMappingImpl() {
		vocabularyMapping = GraphFactory.createDefaultGraph();
	}
	
	
	// Source: https://jena.apache.org/documentation/io/rdf-input.html
	public VocabularyMappingImpl(final String rdfFile) {
		Model mappingModel = RDFDataMgr.loadModel(rdfFile); //.nt file for N-Triple
		vocabularyMapping = mappingModel.getGraph();
	}
	
	public VocabularyMappingImpl(final Set<Triple> triples) {
		Graph defaultGraph = GraphFactory.createDefaultGraph();
		final Iterator<Triple> i = triples.iterator();
		while(i.hasNext()) {
			final Triple t = i.next();
			defaultGraph.add(t.asJenaTriple());
		}
		vocabularyMapping = defaultGraph;
	}

	@Override
	public SPARQLGraphPattern translateTriplePattern(final TriplePattern tp) {
		List<SPARQLGraphPattern> objectTranslation = new ArrayList<SPARQLGraphPattern>();
		for(final TriplePattern i : translateSubject(tp)) {
			objectTranslation.add(translateObject(i));
		}
		
		List<SPARQLGraphPattern> predicateTranslation = new ArrayList<SPARQLGraphPattern>();
		for(final SPARQLGraphPattern j : objectTranslation) {
			
			if(j instanceof SPARQLUnionPattern) {
				
				//PARQLUnionPatternImpl union = new SPARQLUnionPatternImpl();
				List<SPARQLGraphPattern> unionList = new ArrayList<SPARQLGraphPattern>();
				for(final SPARQLGraphPattern k: ((SPARQLUnionPattern) j).getSubPatterns()) {		
					if(k instanceof TriplePattern) {
						/*
						 * Did not work when running test
						 * union.addSubPattern(translatePredicate((TriplePattern) k));
						 */	
						unionList.add(translatePredicate((TriplePattern) k));
					} else if (k instanceof BGP) {
						for(final TriplePattern l : ((BGP) k).getTriplePatterns()) {
							//union.addSubPattern(translatePredicate(l));
							unionList.add(translatePredicate(l));
						}		
					} else {
						throw new IllegalArgumentException(k.getClass().getName());
					}
				}
				final SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl(unionList);
				predicateTranslation.add(union);
				
			} else if (j instanceof BGP) { 
				// try to create a BGP if possible (which is the case if all
				// the graph patterns resulting from the predicate translation
				// are triple patterns); if not possible, then create a group
				// graph pattern
				final List<SPARQLGraphPattern> allSubPatterns = new ArrayList<SPARQLGraphPattern>();
				final Set<TriplePattern> tpSubPatterns = new HashSet<TriplePattern>();
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
			final SPARQLUnionPattern translation = new SPARQLUnionPatternImpl(predicateTranslation);
			return translation;
		} else {
			return predicateTranslation.get(0);
		}
	}
	
	protected Set<TriplePattern> translateSubject(final TriplePattern tp){
		final Set<TriplePattern> results = new HashSet<TriplePattern>();
		final org.apache.jena.graph.Triple t = tp.asJenaTriple();
		if (t.getSubject().isVariable()) {
			results.add(tp);
			return results;
		}
		final Node p = NodeFactory.createVariable("p");
		final Node o = NodeFactory.createVariable("o");
		final TriplePattern tpQuery = new TriplePatternImpl(t.getSubject(), p, o);
		for (final org.apache.jena.graph.Triple m : getMappings(tpQuery)) {
			if (m.getPredicate().getURI().equals(OWL.sameAs.getURI())) {
				final TriplePattern translation = new TriplePatternImpl(m.getObject(), t.getPredicate(), t.getObject());
				results.add(translation);
			} else {
				throw new IllegalArgumentException(m.getPredicate().getURI());
			}
		}
		return results;
	}
	
	protected SPARQLGraphPattern translateObject(final TriplePattern tp){
		final org.apache.jena.graph.Triple t = tp.asJenaTriple();
		if(t.getObject().isVariable()) {
			return tp;
		}
		final Node p = NodeFactory.createVariable("p");
		final Node o = NodeFactory.createVariable("o");
		final TriplePattern tpQuery = new TriplePatternImpl(t.getObject(), p, o);		
		final List<SPARQLGraphPattern> resultsList = new ArrayList<SPARQLGraphPattern>();
		for (final org.apache.jena.graph.Triple m : getMappings(tpQuery)) {
			final String predicate = m.getPredicate().getURI();
			
			if(predicate.equals(OWL.sameAs.getURI())) {
				final TriplePattern translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), m.getObject());
				resultsList.add(translation);
			} else {
				if (!t.getPredicate().isURI()) {
					resultsList.add(tp);
					continue;
				} else {
					if(!t.getPredicate().getURI().equals(RDF.type.getURI())) {
						resultsList.add(tp);
						continue;
					}
				}
				if (predicate.equals(OWL.equivalentClass.getURI())) {
					
					/*
					 * Blank node used to help create intersections and unions
					 * (a equals b)
					 * (b unionof c)
					 * (b unionof d)
					 */
					if (m.getObject().isBlank()) {
						//Union or intersection
						final TriplePattern blankQuery = new TriplePatternImpl(m.getObject(), p, o);
						String newPredicate = null;
						final List<Node> objects = new ArrayList<Node>();
						for(final org.apache.jena.graph.Triple i : getMappings(blankQuery)) {
							final String iPredicate = i.getPredicate().getURI();
							if(newPredicate == null) {
								newPredicate = iPredicate;
							} else if (!iPredicate.equals(newPredicate)) {
								throw new IllegalArgumentException(iPredicate);		
							}
							objects.add(i.getObject());
						}
						
						if (newPredicate.equals(OWL.unionOf.getURI())) {
							//SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl();
							final List<SPARQLGraphPattern> unionList = new ArrayList<SPARQLGraphPattern>();
							for(final Node j : objects){
								final TriplePattern translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), j);
								//union.addSubPattern(translation);
								unionList.add(translation);
							}
							SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl(unionList);
							resultsList.add(union);
							
						} else if (newPredicate.equals(OWL.intersectionOf.getURI())) {
							 Set<TriplePattern> intersectionList = new HashSet<TriplePattern>();;
							 for(Node j : objects){
							  	final TriplePattern translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), j);
							  	intersectionList.add(translation);
							 }
							 final BGP intersection = new BGPImpl(intersectionList);
							 resultsList.add(intersection); 
							 
						} else {
							throw new IllegalArgumentException(newPredicate);
						}		
						
					} else {
						final TriplePattern translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), m.getObject());
						resultsList.add(translation);	
					}
				} else {
					throw new IllegalArgumentException(predicate);
				}
			}
		}
		
		
		if(resultsList.size() == 0) {
			return tp;
		} else if (resultsList.size() > 1) {
			final SPARQLUnionPattern results = new SPARQLUnionPatternImpl(resultsList);
			return results;	
		} else {
			return resultsList.get(0);
		}
		
	}
	

	protected SPARQLGraphPattern translatePredicate(final TriplePattern tp){
		org.apache.jena.graph.Triple t = tp.asJenaTriple();
		if(t.getPredicate().isVariable()) {
			return tp;
		}
		final Node p = NodeFactory.createVariable("p");
		final Node o = NodeFactory.createVariable("o");
		final TriplePattern tpQuery = new TriplePatternImpl(t.getPredicate(), p, o);
		List<SPARQLGraphPattern> resultsList = new ArrayList<SPARQLGraphPattern>();
		for (final org.apache.jena.graph.Triple m : getMappings(tpQuery)) {
			String predicate = m.getPredicate().getURI();
			
			if (predicate.equals(RDFS.subPropertyOf.getURI())) {
				final TriplePattern translation = new TriplePatternImpl(t.getSubject(), m.getObject(), t.getObject());
				resultsList.add(translation);
				
			} else if (predicate.equals(OWL.inverseOf.getURI())){
				final TriplePattern translation = new TriplePatternImpl(t.getObject(), m.getObject(), t.getSubject());
				resultsList.add(translation);	
				
			} else if (predicate.equals(OWL.equivalentProperty.getURI())){
				if (m.getObject().isBlank()) {
					//Union or intersection
					final TriplePattern blankQuery = new TriplePatternImpl(m.getObject(), p, o);
					String newPredicate = "";
					List<Node> objects = new ArrayList<Node>();
					for(final org.apache.jena.graph.Triple i : getMappings(blankQuery)) {
						String iPredicate = i.getPredicate().getURI();
						if(newPredicate.equals("")) {
							newPredicate = iPredicate;
						} else {
							if (!iPredicate.equals(newPredicate)) {
								throw new IllegalArgumentException(iPredicate);
							}
						}
						objects.add(i.getObject());
					}
					
					if (newPredicate.equals(OWL.unionOf.getURI())) {
						SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl();
						for(Node j : objects){
							final TriplePattern translation = new TriplePatternImpl(t.getSubject(), j, t.getObject());
							union.addSubPattern(translation);
						}
						resultsList.add(union);
						
					} else if (newPredicate.equals(OWL.intersectionOf.getURI())) {
						 Set<TriplePattern> intersectionList = new HashSet<TriplePattern>();;
						 for(Node j : objects){
						  	final TriplePattern translation = new TriplePatternImpl(t.getSubject(), j,  t.getObject());
						  	intersectionList.add(translation);
						 }
						 final BGP intersection = new BGPImpl(intersectionList);
						 resultsList.add(intersection); 
						 
					} else {
						throw new IllegalArgumentException(newPredicate);
					}		
					
				} else {
					final TriplePattern translation = new TriplePatternImpl(t.getSubject(), m.getObject(), t.getObject());
					resultsList.add(translation);	
				}
				
			} else {
				throw new IllegalArgumentException(predicate);
			}
		}
		
		if(resultsList.size() == 0) {
			return tp;
		} else if (resultsList.size() > 1) {
			final SPARQLUnionPattern results = new SPARQLUnionPatternImpl(resultsList);
			return results;	
		} else {
			return resultsList.get(0);
		}
	}
	
	protected Set<org.apache.jena.graph.Triple> getMappings(final TriplePattern tp){
		Set<org.apache.jena.graph.Triple> mappings = new HashSet<org.apache.jena.graph.Triple>();
		final Iterator<org.apache.jena.graph.Triple> i = this.vocabularyMapping.find(tp.asJenaTriple());
		while(i.hasNext()) {
			mappings.add(i.next());
		}
		return mappings;
	}

	public Graph getVocabularyMappingAsGraph() {
		return this.vocabularyMapping;
	}

}
