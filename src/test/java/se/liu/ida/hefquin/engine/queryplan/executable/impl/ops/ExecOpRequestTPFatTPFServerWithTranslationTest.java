package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.OWL;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.data.impl.TripleImpl;
import se.liu.ida.hefquin.engine.data.impl.VocabularyMappingImpl;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.MaterializingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;

public class ExecOpRequestTPFatTPFServerWithTranslationTest extends ExecOpTestBase
{

	@Test
	public void testTriplePatternTranslationForTPFRequest() throws ExecOpExecutionException {
		// This test simulates a TPF server with the following two triples that use a local vocabulary:
		//   (ex:s, ex:p, ex:o1)
		//   (ex:s, ex:p, ex:o2)
		// The corresponding vocabulary mapping defines that the URI ex:s in the local
		// vocabulary is owl:sameAs the URI ex:a in the global vocabulary. Then, the test
		// checks that a request with the triple pattern (ex:a, ex:p, ?v) results in two solution
		// mappings, one for each of the triples of the simulated server.

		//Query
		final Node a = NodeFactory.createURI("http://example.org/a");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Var v = Var.alloc("v");
		final TriplePattern tp = new TriplePatternImpl(a,p,v);
		
		//Data
		final Graph g = GraphFactory.createDefaultGraph();
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node o1 = NodeFactory.createURI("http://example.org/o1");
		g.add(s,p,o1);

		final Node o2 = NodeFactory.createURI("http://example.org/o2");
		g.add(s,p,o2);
		
		final ExecOpRequestTPFatTPFServerWithTranslation op = new ExecOpRequestTPFatTPFServerWithTranslation(
				new TriplePatternRequestImpl(tp),
				new TPFServerWithVocabularyMappingForTest(g, createVocabularyMappingForTests()) );
		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();

		op.execute( sink, createExecContextForTests() );
		
		//Expected results
		final Set<SolutionMapping> expectedResults = new HashSet<>();
		final BindingBuilder first = BindingBuilder.create();
		first.add(Var.alloc("v"), NodeFactory.createURI("http://example.org/o1"));
		expectedResults.add(new SolutionMappingImpl(first.build()));
		
		final BindingBuilder second = BindingBuilder.create();
		second.add(Var.alloc("v"), NodeFactory.createURI("http://example.org/o2"));
		expectedResults.add(new SolutionMappingImpl(second.build()));

		//Results
		final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();		
		final Set<SolutionMapping> results = new HashSet<>();
		while (it.hasNext()) {
			results.add(it.next());
		}
		
		assertEquals(expectedResults, results);
	}
	
	@Test
	public void testSolutionTranslationForTPFRequest() throws ExecOpExecutionException {
		// This test simulates a TPF server with the following triple that use a local vocabulary:
		//   (ex:s, ex:p, ex:o)
		// The corresponding vocabulary mapping defines that the URI ex:s in the local
		// vocabulary is owl:sameAs the URI ex:a in the global vocabulary. Then, the test
		// checks that a request with the triple pattern (ex:?v, ex:p, o) results in one solution
		// mapping, corresponding to the triple of the simulated server, expressed
		// in the global vocabulary

		//Query
		final Var v = Var.alloc("v");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final TriplePattern tp = new TriplePatternImpl(v, p, o);
		
		//Data
		final Graph g = GraphFactory.createDefaultGraph();
		final Node s = NodeFactory.createURI("http://example.org/s");
		g.add(s,p,o);

		
		final ExecOpRequestTPFatTPFServerWithTranslation op = new ExecOpRequestTPFatTPFServerWithTranslation(
				new TriplePatternRequestImpl(tp),
				new TPFServerWithVocabularyMappingForTest(g, createVocabularyMappingForTests()) );
		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();

		op.execute( sink, createExecContextForTests() );
		
		//Expected results
		final Set<SolutionMapping> expectedResults = new HashSet<>();
		final BindingBuilder first = BindingBuilder.create();
		first.add(Var.alloc("v"), NodeFactory.createURI("http://example.org/a"));
		expectedResults.add(new SolutionMappingImpl(first.build()));

		//Results
		final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();		
		final Set<SolutionMapping> results = new HashSet<>();
		while (it.hasNext()) {
			results.add(it.next());
		}
		
		assertEquals(expectedResults, results);
	}


	public static ExecutionContext createExecContextForTests() {

		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();
		return new ExecutionContext() {
			@Override public FederationCatalog getFederationCatalog() { return null; }
			@Override public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }
			@Override public CostModel getCostModel() { return null; }
			@Override public boolean isExperimentRun() { return false; }
		};
	}
	
	public static VocabularyMapping createVocabularyMappingForTests() {
		final Set<org.apache.jena.graph.Triple> mappingTriples = new HashSet<>();
		//Equality
		Node s = NodeFactory.createURI("http://example.org/a");
		Node p = OWL.sameAs.asNode();
		Node o  = NodeFactory.createURI("http://example.org/s");
		mappingTriples.add(new org.apache.jena.graph.Triple(s, p, o));
		
		return new VocabularyMappingImpl(mappingTriples);
	}

}
