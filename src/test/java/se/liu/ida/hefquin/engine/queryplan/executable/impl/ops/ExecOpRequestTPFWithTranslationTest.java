package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.OWL;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.data.impl.VocabularyMappingImpl;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;

public class ExecOpRequestTPFWithTranslationTest extends ExecOpTestBase {

	@Test
	public void testOffline() throws ExecOpExecutionException {
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
		
		final ExecOpRequestTPFatBRTPFServerWithTranslation op = new ExecOpRequestTPFatBRTPFServerWithTranslation(
				new TriplePatternRequestImpl(tp),
				new BRTPFServerWithVocabularyMappingForTest(g, createVocabularyMappingForTests()) );
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

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
		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();		
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
			@Override public ExecutorService getExecutorServiceForPlanTasks() { return null; }
			@Override public CostModel getCostModel() { return null; }
			@Override public boolean isExperimentRun() { return false; }
		};
	}
	
	public static VocabularyMapping createVocabularyMappingForTests() {
		final Set<org.apache.jena.graph.Triple> mappingTriples = new HashSet<>();
		//Equality
		final Node s = NodeFactory.createURI("http://example.org/a");
		final Node p = OWL.sameAs.asNode();
		final Node o  = NodeFactory.createURI("http://example.org/s");
		mappingTriples.add(new org.apache.jena.graph.Triple(s, p, o));
		
		return new VocabularyMappingImpl(mappingTriples);
	}

}
