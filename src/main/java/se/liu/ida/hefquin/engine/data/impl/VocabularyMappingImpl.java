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
		Set<TriplePattern> subjectTranslation = translateSubject(tp);
		Iterator<TriplePattern> i = subjectTranslation.iterator();
		List<SPARQLGraphPattern> objectTranslation = Collections.<SPARQLGraphPattern>emptyList();
		while(i.hasNext()) {
			TriplePattern tpNext = i.next();
			objectTranslation.add(translateObject(tpNext));
		}
		List<SPARQLGraphPattern> predicateTranslation = Collections.<SPARQLGraphPattern>emptyList();
		for(SPARQLGraphPattern j : objectTranslation) {
			
			if(j instanceof SPARQLUnionPattern) { 
				List<SPARQLGraphPattern> unionList = Collections.<SPARQLGraphPattern>emptyList();
				for(SPARQLGraphPattern k: ((SPARQLUnionPattern) j).getSubPatterns()) {
					if(k instanceof TriplePattern) {
						unionList.add(translatePredicate((TriplePattern) k));
					} else if (k instanceof BGP) {
						// Iterator<TriplePattern> l = ((BGP)k).getTriplePatterns().iterator(); Does not work need to be:
						Iterator<? extends TriplePattern> l = ((BGP) k).getTriplePatterns().iterator(); //why?
						while(l.hasNext()) {
							unionList.add(translatePredicate(l.next()));
						}
					}
				}
				SPARQLUnionPattern union = new SPARQLUnionPatternImpl(unionList);
				predicateTranslation.add(union);
				
			} else if (j instanceof BGP) { 
				/**
				 * TODO: This should be able to create an intersection, BGP, between a set of triples and a union.
				 * How can this be done?
				 * For example if one of the triples of the BGP translate to a new BGP and one to a union 
				 * The results would be an intersection between the new BGP and union? Not a Union of the two
				 */
				List<SPARQLGraphPattern> unionList = Collections.<SPARQLGraphPattern>emptyList();
				Iterator<? extends TriplePattern> m = ((BGP) m).getTriplePatterns().iterator();
				while(m.hasNext()) {
					unionList.add(translatePredicate(m.next()));
				}
				SPARQLUnionPattern union = new SPARQLUnionPatternImpl(unionList);
				predicateTranslation.add(union);
				
			} else if (j instanceof TriplePattern) {
				predicateTranslation.add(translatePredicate((TriplePattern) j));
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
		Set<Triple> mappings = getMappings(tpQuery);
		
		/* OR ?
		 * Selector selector = new SimpleSelector(tp.getSubject(), null, null);
		 * Iterator i = vocabularyMapping.listStatements(selector);
		 */
		
		Iterator<Triple> i = mappings.iterator();
		while (i.hasNext()) {
			org.apache.jena.graph.Triple m = i.next().asJenaTriple();
			if (m.getPredicate().getURI().equals(OWL.sameAs.getURI())) {
				TriplePattern translation = new TriplePatternImpl(m.getObject(), t.getPredicate(), t.getObject());
				results.add(translation);
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
		Set<Triple> mappings = getMappings(tpQuery);
		Iterator<Triple> i = mappings.iterator();
		
		List<SPARQLGraphPattern> resultsList = Collections.<SPARQLGraphPattern>emptyList();
		while (i.hasNext()) {
			org.apache.jena.graph.Triple m = i.next().asJenaTriple();
			String predicate = m.getPredicate().getURI();
			
			if (predicate.equals(OWL.sameAs.getURI()) || predicate.equals(RDFS.subClassOf.getURI()) || predicate.equals(OWL.equivalentClass.getURI())) {
				TriplePattern translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), m.getObject());
				resultsList.add(translation);
				
			} else if (predicate.equals(OWL.unionOf.getURI())){
				/**
				 * TODO: How are the union represented as triples
				 * = How to add multiple object to a triple, do I represent them with multiple triples?
				 */
				/** 
				 * List<SPARQLGraphPattern> unionList = Collections.<SPARQLGraphPattern>emptyList();
				 * for(i : m.getObject()){
				 * 		TriplePattern translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), i);
				 * 		unionList.add(translation)
				 * }
				 * SPARQLUnionPattern union = new SPARQLUnionPatternImpl(unionList);
				 * results.add(union);
				 */
				
			} else if (predicate.equals(OWL.intersectionOf.getURI())) {
				/**
				 * TODO: How are the union represented as triples
				 * = How to add multiple object to a triple, do I represent them with multiple triples?
				 */
				/** 
				 * List<TriplePattern> intersectionList = Collections.<TriplePattern>emptyList();
				 * for(i : m.getObject()){
				 * 		TriplePattern translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), i);
				 * 		intersectionList.add(translation)
				 * }
				 * BGP intersection = new BGPImpl(intersectionList);
				 * results.add(intersection);
				 */
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
		Set<Triple> mappings = getMappings(tpQuery);
		Iterator<Triple> i = mappings.iterator();
		
		List<SPARQLGraphPattern> resultsList = Collections.<SPARQLGraphPattern>emptyList();
		while (i.hasNext()) {
			org.apache.jena.graph.Triple m = i.next().asJenaTriple();
			String predicate = m.getPredicate().getURI();
			
			if (predicate.equals(OWL.equivalentProperty.getURI()) || predicate.equals(RDFS.subPropertyOf.getURI())) {
				TriplePattern translation = new TriplePatternImpl(t.getSubject(), m.getObject(), t.getObject());
				resultsList.add(translation);
				
			} else if (predicate.equals(OWL.inverseOf.getURI())){
				TriplePattern translation = new TriplePatternImpl(t.getObject(), m.getObject(), t.getSubject());
				resultsList.add(translation);	
				
			}else if (predicate.equals(OWL.unionOf.getURI())){
				/**
				 * TODO: How are the union represented as triples
				 * = How to add multiple object to a triple, do I represent them with multiple triples?
				 */
				/** 
				 * List<SPARQLGraphPattern> unionList = Collections.<SPARQLGraphPattern>emptyList();
				 * for(i : m.getObject()){
				 * 		TriplePattern translation = new TriplePatternImpl(t.getSubject(), i, t.getObject());
				 * 		unionList.add(translation)
				 * }
				 * SPARQLUnionPattern union = new SPARQLUnionPatternImpl(unionList);
				 * results.add(union);
				 */
				
			} else if (predicate.equals(OWL.intersectionOf.getURI())) {
				/**
				 * TODO: How are the union represented as triples
				 * = How to add multiple object to a triple, do I represent them with multiple triples?
				 */
				/** 
				 * List<TriplePattern> intersectionList = Collections.<TriplePattern>emptyList();
				 * for(i : m.getObject()){
				 * 		TriplePattern translation = new TriplePatternImpl(t.getSubject(), i, t.getObject());
				 * 		intersectionList.add(translation)
				 * }
				 * BGP intersection = new BGPImpl(intersectionList);
				 * results.add(intersection);
				 */
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
