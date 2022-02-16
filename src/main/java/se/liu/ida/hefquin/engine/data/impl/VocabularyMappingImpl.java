package se.liu.ida.hefquin.engine.data.impl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;

public class VocabularyMappingImpl implements VocabularyMapping{
	
	Model vocabularyMapping;
	
	public VocabularyMappingImpl() {
		vocabularyMapping = ModelFactory.createDefaultModel();
	}
	
	
	// Source: https://jena.apache.org/documentation/io/rdf-input.html
	public VocabularyMappingImpl(final String rdfFile) {
		vocabularyMapping = RDFDataMgr.loadModel(rdfFile); //.nt file for N-Triple
	}
	
}
