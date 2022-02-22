package se.liu.ida.hefquin.engine.data.impl;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;

import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class VocabularyMappingImpl implements VocabularyMapping{
	
	protected final Model vocabularyMapping;
	
	public VocabularyMappingImpl() {
		vocabularyMapping = ModelFactory.createDefaultModel();
	}
	
	
	// Source: https://jena.apache.org/documentation/io/rdf-input.html
	public VocabularyMappingImpl(final String rdfFile) {
		vocabularyMapping = RDFDataMgr.loadModel(rdfFile); //.nt file for N-Triple
	}
	
	public VocabularyMappingImpl(final Set<Triple> triples) {
		vocabularyMapping = ModelFactory.createDefaultModel();
		final Iterator<Triple> i = triples.iterator();
		while(i.hasNext()) {
			final Triple t = i.next();
			vocabularyMapping.asStatement(t.asJenaTriple());
		}
	}

	@Override
	public SPARQLGraphPattern translateTriplePattern(final TriplePattern tp) {
		return null;
	}
	
	protected Set<TriplePattern> translateSubject(final TriplePattern tp){
		return null;
	}
	
	protected Set<SPARQLGraphPattern> translateObject(final TriplePattern tp){
		return null;
	}
	
	protected Set<SPARQLGraphPattern> translatePredicate(final TriplePattern tp){
		return null;
	}
	
	protected Set<Triple> getMappings(TriplePattern tp){
		return null;
	}

}
