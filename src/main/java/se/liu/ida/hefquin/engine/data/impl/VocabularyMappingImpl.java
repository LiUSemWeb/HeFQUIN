package se.liu.ida.hefquin.engine.data.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.TriplePattern;
import org.apache.jena.riot.RDFDataMgr;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;

public class VocabularyMappingImpl implements VocabularyMapping{
	
	protected final Model vocabularyMapping;
	
	public VocabularyMappingImpl() {
		vocabularyMapping = ModelFactory.createDefaultModel();
	}
	
	
	// Source: https://jena.apache.org/documentation/io/rdf-input.html
	public VocabularyMappingImpl(final String rdfFile) {
		vocabularyMapping = RDFDataMgr.loadModel(rdfFile); //.nt file for N-Triple
	}
	
	public VocabularyMappingImpl(final Set<TriplePattern> tps) {
		vocabularyMapping = ModelFactory.createDefaultModel();
		Iterator<TriplePattern> i = tps.iterator();
		while(i.hasNext()) {
			TriplePattern tp = i.next();
			Resource s = vocabularyMapping.createResource(tp.getSubject().toString());
			Property p = vocabularyMapping.createProperty(tp.getPredicate().toString());
			RDFNode o = vocabularyMapping.createResource(tp.getObject().toString());
			Statement statement = vocabularyMapping.createStatement(s, p, o);
			vocabularyMapping.add(statement);
		}
	}

	@Override
	//TODO: Which data structure to use? Unions and intersections also need to be represented
	public Set<TriplePattern> translateTriple(TriplePattern tp) {
		Set<TriplePattern> results = translateSubject(tp);
		return results;
	}
	
	private Set<TriplePattern> translateSubject(TriplePattern tp){
		Set<TriplePattern> results = Collections.emptySet();
		if (tp.getSubject().isVariable()) {
			results.add(tp);
			return results;
		}
		Node p = NodeFactory.createVariable("p");
		Node o = NodeFactory.createVariable("o");
		TriplePattern tpQuery = new TriplePattern(tp.getSubject(), p, o);
		Set<TriplePattern> mappings = getMappings(tpQuery);
		
		/* OR ?
		 * Selector selector = new SimpleSelector(tp.getSubject(), null, null);
		 * Iterator i = vocabularyMapping.listStatements(selector);
		 */
		
		Iterator<TriplePattern> i = mappings.iterator();
		while (i.hasNext()) {
			TriplePattern m = i.next();
			if (m.getPredicate().toString() == "owl:sameAs") {
				TriplePattern translation = new TriplePattern(m.getObject(), tp.getPredicate(), tp.getObject());
				results.add(translation);
			}
		}
		return results;
	}
	
	private Set<TriplePattern> getMappings(TriplePattern tp){
		Set<TriplePattern> mappings = Collections.emptySet();
		String stringQuery = "SELECT * WHERE { " + tp.toString() + " }";
		Query query = QueryFactory.create(stringQuery);
		QueryExecution qExec = QueryExecutionFactory.create(query, vocabularyMapping);
		ResultSet results = qExec.execSelect();
		while(results.hasNext()) {
			QuerySolution mapping = results.nextSolution();
			Iterator<String> i = mapping.varNames();
			Node s,p,o;
			s = p = o = null;    //To remove "variable may not be initialized error"
			Integer count = 0; 
			while(i.hasNext()) {
				if (count == 0) {
					s = mapping.get(i.next()).asNode();
				} else if (count == 1) {
					p = mapping.get(i.next()).asNode();
				} else {
					o = mapping.get(i.next()).asNode();
				}
				count ++;
			}
			
			TriplePattern m = new TriplePattern(s, p, o);
			mappings.add(m);	
		}
		return mappings;
	}

}
