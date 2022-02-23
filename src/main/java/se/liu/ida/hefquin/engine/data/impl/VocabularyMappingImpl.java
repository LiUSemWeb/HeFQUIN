package se.liu.ida.hefquin.engine.data.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
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
	//source: https://jena.apache.org/documentation/query/manipulating_sparql_using_arq.html
	public SPARQLGraphPattern translateTriplePattern(final TriplePattern tp) {
		//Triples -> ElementTriplesBlock or ElementUnion -> ElementGroup -> SPARQLGraphPattern
		Set<TriplePattern> subjectTranslation = translateSubject(tp);
		Iterator<TriplePattern> i = subjectTranslation.iterator();
		ElementUnion objectTranslation = new ElementUnion();
		while(i.hasNext()) {
			TriplePattern tpNext = i.next();
			//How do I loop through the resulting triples if this is a SPARQLGraphPattern?
			objectTranslation.addElement(translateObject(tpNext));
		}
		ElementUnion finalTranslation = new ElementUnion();
		for(Element j : objectTranslation.getElements()) {
			if(j instanceof ElementUnion) {  //TODO: Check if I need one more "depth" of union
				ElementUnion predicateTranslation = new ElementUnion();
				for(Element k: ((ElementGroup) j).getElements()) {
					Iterator<org.apache.jena.graph.Triple> l = ((ElementTriplesBlock) k).patternElts();
					while(l.hasNext()) {
						TriplePattern ltp = new TriplePatternImpl(l.next());
						predicateTranslation.addElement(translatePredicate(ltp));
					}
				}
				finalTranslation.addElement(predicateTranslation);
			} else if (j instanceof ElementTriplesBlock) {
				Iterator<org.apache.jena.graph.Triple> m = ((ElementTriplesBlock) j).patternElts();
				while(m.hasNext()) {
					TriplePattern mtp = new TriplePatternImpl(m.next());
					finalTranslation.addElement(translatePredicate(mtp));
				}
			}
		}
		SPARQLGraphPattern translation = new SPARQLGraphPatternImpl(finalTranslation);
		return translation;
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
	
	//Used to return SPARQLGraphPattern
	protected Element translateObject(final TriplePattern tp){
		org.apache.jena.graph.Triple t = tp.asJenaTriple();
		if(t.getObject().isVariable() || !t.getPredicate().getName().equals(RDF.type.getURI())) {
			ElementTriplesBlock e = new ElementTriplesBlock();
			e.addTriple(t);
			return e;
		}
		Node p = NodeFactory.createVariable("p");
		Node o = NodeFactory.createVariable("o");
		TriplePattern tpQuery = new TriplePatternImpl(t.getObject(), p, o);
		Set<Triple> mappings = getMappings(tpQuery);
		Iterator<Triple> i = mappings.iterator();
		ElementUnion results = new ElementUnion();
		while (i.hasNext()) {
			org.apache.jena.graph.Triple m = i.next().asJenaTriple();
			String predicate = m.getPredicate().getURI();
			if (predicate.equals(OWL.sameAs.getURI()) || predicate.equals(RDFS.subClassOf.getURI()) || predicate.equals(OWL.equivalentClass.getURI())) {
				org.apache.jena.graph.Triple translation = new TriplePatternImpl(t.getSubject(), t.getPredicate(), m.getObject()).asJenaTriple();
				ElementTriplesBlock block = new ElementTriplesBlock();
				block.addTriple(translation);
				results.addElement(block);
			} else if (predicate.equals(OWL.unionOf.getURI())){
				ElementUnion union = new ElementUnion();
				//TODO: How are the union represented as triples
				results.addElement(union);
			} else if (predicate.equals(OWL.intersectionOf.getURI())) {
				ElementTriplesBlock block = new ElementTriplesBlock();
				//TODO: How are the intersections represented as triples
				results.addElement(block);
			}
		}
		if (results.getElements().size() > 1) {
			return results;	
		} else {
			return results.getElements().get(0);
		}
	}
	
	//Used to return SPARQLGraphPattern
	protected Element translatePredicate(final TriplePattern tp){
		org.apache.jena.graph.Triple t = tp.asJenaTriple();
		if(t.getPredicate().isVariable()) {
			ElementTriplesBlock e = new ElementTriplesBlock();
			e.addTriple(t);
			return e;
		}
		Node p = NodeFactory.createVariable("p");
		Node o = NodeFactory.createVariable("o");
		TriplePattern tpQuery = new TriplePatternImpl(t.getObject(), p, o);
		Set<Triple> mappings = getMappings(tpQuery);
		Iterator<Triple> i = mappings.iterator();
		ElementUnion results = new ElementUnion();
		while (i.hasNext()) {
			org.apache.jena.graph.Triple m = i.next().asJenaTriple();
			String predicate = m.getPredicate().getURI();
			if (predicate.equals(OWL.equivalentProperty.getURI()) || predicate.equals(RDFS.subPropertyOf.getURI())) {
				org.apache.jena.graph.Triple translation = new TriplePatternImpl(t.getSubject(), m.getObject(), t.getObject()).asJenaTriple();
				ElementTriplesBlock block = new ElementTriplesBlock();
				block.addTriple(translation);
				results.addElement(block);
			} else if (predicate.equals(OWL.inverseOf.getURI())){
				org.apache.jena.graph.Triple translation = new TriplePatternImpl(t.getObject(), m.getObject(), t.getSubject()).asJenaTriple();
				ElementTriplesBlock block = new ElementTriplesBlock();
				block.addTriple(translation);
				results.addElement(block);	
			}else if (predicate.equals(OWL.unionOf.getURI())){
				ElementUnion union = new ElementUnion();
				//TODO: How are the union represented as triples
				results.addElement(union);
			} else if (predicate.equals(OWL.intersectionOf.getURI())) {
				ElementTriplesBlock block = new ElementTriplesBlock();
				//TODO: How are the intersections represented as triples
				results.addElement(block);
			}
		}
		if (results.getElements().size() > 1) {
			return results;	
		} else {
			return results.getElements().get(0);
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
