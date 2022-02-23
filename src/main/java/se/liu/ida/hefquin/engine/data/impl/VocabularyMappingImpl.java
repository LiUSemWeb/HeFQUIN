package se.liu.ida.hefquin.engine.data.impl;

import java.util.Collections;
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
		List<SPARQLGraphPattern> objectTranslation = Collections.<SPARQLGraphPattern>emptyList();
		for(final TriplePattern i : translateSubject(tp)) {
			objectTranslation.add(translateObject(i));
		}
		
		List<SPARQLGraphPattern> predicateTranslation = Collections.<SPARQLGraphPattern>emptyList();
		for(final SPARQLGraphPattern j : objectTranslation) {
			
			if(j instanceof SPARQLUnionPattern) {
				SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl();
				for(final SPARQLGraphPattern k: ((SPARQLUnionPattern) j).getSubPatterns()) {		
					if(k instanceof TriplePattern) {
						union.addSubPattern(translatePredicate((TriplePattern) k));	
					} else if (k instanceof BGP) {
						for(final TriplePattern l : ((BGP) k).getTriplePatterns()) {
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
				final List<SPARQLGraphPattern> allSubPatterns = Collections.emptyList();
				final Set<TriplePattern> tpSubPatterns = Collections.emptySet();
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
			SPARQLUnionPattern translation = new SPARQLUnionPatternImpl(predicateTranslation);
			return translation;
		} else {
			return predicateTranslation.get(0);
		}
	}
	
	protected Set<TriplePattern> translateSubject(final TriplePattern tp){
		Set<TriplePattern> results = Collections.emptySet();
		org.apache.jena.graph.Triple t = tp.asJenaTriple();
		if (t.getSubject().isVariable()) {
			results.add(tp);
			return results;
		}
		Node p = NodeFactory.createVariable("p");
		Node o = NodeFactory.createVariable("o");
		TriplePattern tpQuery = new TriplePatternImpl(t.getSubject(), p, o);
		for (final Triple m : getMappings(tpQuery)) {
			if (m.asJenaTriple().getPredicate().getURI().equals(OWL.sameAs.getURI())) {
				TriplePattern translation = new TriplePatternImpl(m.asJenaTriple().getObject(), t.getPredicate(), t.getObject());
				results.add(translation);
			} else {
				throw new IllegalArgumentException(m.asJenaTriple().getPredicate().getURI());
			}
		}
		return results;
	}
	
	protected SPARQLGraphPattern translateObject(final TriplePattern tp){
		org.apache.jena.graph.Triple t = tp.asJenaTriple();
		if(t.getObject().isVariable() || !t.getPredicate().getURI().equals(RDF.type.getURI())) {
			return tp;
		}
		Node p = NodeFactory.createVariable("p");
		Node o = NodeFactory.createVariable("o");
		TriplePattern tpQuery = new TriplePatternImpl(t.getObject(), p, o);		
		List<SPARQLGraphPattern> resultsList = Collections.<SPARQLGraphPattern>emptyList();
		for (final Triple m : getMappings(tpQuery)) {
			String predicate = m.asJenaTriple().getPredicate().getURI();
			
			/** OLD Version
			if (predicate.equals(OWL.sameAs.getURI()) || predicate.equals(RDFS.subClassOf.getURI()) || predicate.equals(OWL.equivalentClass.getURI())) {
				TriplePattern translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), m.asJenaTriple().getObject());
				resultsList.add(translation);	
			**/
			
			if(predicate.equals(OWL.sameAs.getURI())) {
				TriplePattern translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), m.asJenaTriple().getObject());
				resultsList.add(translation);
			} else if (predicate.equals(OWL.equivalentClass.getURI())) {
				
				/*
				 * Blank node used to help create intersections and unions
				 * (a equals b)
				 * (b unionof c)
				 * (b unionof d)
				 */
				if (m.asJenaTriple().getObject().isBlank()) {
					//Union or intersection
					Node blankP = NodeFactory.createVariable("p");
					Node blankO = NodeFactory.createVariable("o");
					TriplePattern blankQuery = new TriplePatternImpl(m.asJenaTriple().getObject(), blankP, blankO);
					String newPredicate = "";
					List<Node> objects = Collections.emptyList();
					for(final Triple i : getMappings(blankQuery)) {
						String iPredicate = i.asJenaTriple().getPredicate().getURI();
						if(newPredicate.equals("")) {
							newPredicate = iPredicate;
						} else {
							if (!iPredicate.equals(newPredicate)) {
								throw new IllegalArgumentException(iPredicate);
							}
						}
						objects.add(i.asJenaTriple().getObject());
					}
					
					if (newPredicate.equals(OWL.unionOf.getURI())) {
						SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl();
						for(Node j : objects){
							TriplePattern translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), j);
							union.addSubPattern(translation);
						}
						resultsList.add(union);
						
					} else if (newPredicate.equals(OWL.intersectionOf.getURI())) {
						 Set<TriplePattern> intersectionList = Collections.emptySet();
						 for(Node j : objects){
						  	TriplePattern translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), j);
						  	intersectionList.add(translation);
						 }
						 BGP intersection = new BGPImpl(intersectionList);
						 resultsList.add(intersection); 
						 
					} else {
						throw new IllegalArgumentException(newPredicate);
					}		
					
				} else {
					TriplePattern translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), m.asJenaTriple().getObject());
					resultsList.add(translation);	
				}
				
			} else {
				throw new IllegalArgumentException(predicate);
			}
		}
		
		if (resultsList.size() > 1) {
			SPARQLUnionPattern results = new SPARQLUnionPatternImpl(resultsList);
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
		Node p = NodeFactory.createVariable("p");
		Node o = NodeFactory.createVariable("o");
		TriplePattern tpQuery = new TriplePatternImpl(t.getObject(), p, o);
		List<SPARQLGraphPattern> resultsList = Collections.<SPARQLGraphPattern>emptyList();
		for (final Triple m : getMappings(tpQuery)) {
			String predicate = m.asJenaTriple().getPredicate().getURI();
			
			if (predicate.equals(RDFS.subPropertyOf.getURI())) {
				TriplePattern translation = new TriplePatternImpl(t.getSubject(), m.asJenaTriple().getObject(), t.getObject());
				resultsList.add(translation);
				
			} else if (predicate.equals(OWL.inverseOf.getURI())){
				TriplePattern translation = new TriplePatternImpl(t.getObject(), m.asJenaTriple().getObject(), t.getSubject());
				resultsList.add(translation);	
				
			} else if (predicate.equals(OWL.equivalentProperty.getURI())){
				if (m.asJenaTriple().getObject().isBlank()) {
					//Union or intersection
					Node blankP = NodeFactory.createVariable("p");
					Node blankO = NodeFactory.createVariable("o");
					TriplePattern blankQuery = new TriplePatternImpl(m.asJenaTriple().getObject(), blankP, blankO);
					String newPredicate = "";
					List<Node> objects = Collections.emptyList();
					for(final Triple i : getMappings(blankQuery)) {
						String iPredicate = i.asJenaTriple().getPredicate().getURI();
						if(newPredicate.equals("")) {
							newPredicate = iPredicate;
						} else {
							if (!iPredicate.equals(newPredicate)) {
								throw new IllegalArgumentException(iPredicate);
							}
						}
						objects.add(i.asJenaTriple().getObject());
					}
					
					if (newPredicate.equals(OWL.unionOf.getURI())) {
						SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl();
						for(Node j : objects){
							TriplePattern translation = new TriplePatternImpl(t.getSubject(), j, t.getObject());
							union.addSubPattern(translation);
						}
						resultsList.add(union);
						
					} else if (newPredicate.equals(OWL.intersectionOf.getURI())) {
						 Set<TriplePattern> intersectionList = Collections.emptySet();
						 for(Node j : objects){
						  	TriplePattern translation = new TriplePatternImpl(t.getSubject(), j,  t.getObject());
						  	intersectionList.add(translation);
						 }
						 BGP intersection = new BGPImpl(intersectionList);
						 resultsList.add(intersection); 
						 
					} else {
						throw new IllegalArgumentException(newPredicate);
					}		
					
				} else {
					TriplePattern translation = new TriplePatternImpl(t.getSubject(), m.asJenaTriple().getObject(), t.getObject());
					resultsList.add(translation);	
				}
				
			} else {
				throw new IllegalArgumentException(predicate);
			}
		}
		
		if (resultsList.size() > 1) {
			SPARQLUnionPattern results = new SPARQLUnionPatternImpl(resultsList);
			return results;	
		} else {
			return resultsList.get(0);
		}
	}
	
	protected Set<Triple> getMappings(final TriplePattern tp){
		Set<Triple> mappings = Collections.emptySet();
		final Iterator<org.apache.jena.graph.Triple> i = this.vocabularyMapping.find(tp.asJenaTriple());
		while(i.hasNext()) {
			Triple t = new TripleImpl(i.next());
			mappings.add(t);
		}
		return mappings;
	}

}
