package se.liu.ida.hefquin.engine.queryproc.impl.srcsel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

public abstract class SourcePlannerImplTestBase extends EngineTestBase
{
	protected abstract SourcePlanner createSourcePlanner(QueryProcContext ctxt);

	protected LogicalPlan createLogicalPlan( final String queryString,
	                                         final FederationCatalog fedCat )
			 throws SourcePlanningException
	{
		final QueryProcContext ctxt = new QueryProcContext() {
			@Override public FederationCatalog getFederationCatalog() { return fedCat; }
			@Override public FederationAccessManager getFederationAccessMgr() { throw new UnsupportedOperationException(); }
			@Override public ExecutorService getExecutorServiceForPlanTasks() { throw new UnsupportedOperationException(); }
			@Override public boolean isExperimentRun() { throw new UnsupportedOperationException(); }
			@Override public boolean skipExecution() { return false; }
		};

		final Query query = new GenericSPARQLGraphPatternImpl1( QueryFactory.create(queryString).getQueryPattern() );
		return createSourcePlanner(ctxt).createSourceAssignment(query).object1;
	}

	public static void assertEqualTriplePatternsVUV( final String expectedSubjectVarName,
	                                                 final String expectedPredicateURI,
	                                                 final String expectedObjectVarName,
	                                                 final TriplePatternRequest actualTriplePatternRequest ) {
		assertEquals( 2, actualTriplePatternRequest.getQueryPattern().numberOfVars() );
		assertEqualTriplePatternsVUV( expectedSubjectVarName,
		                              expectedPredicateURI,
		                              expectedObjectVarName,
		                              actualTriplePatternRequest.getQueryPattern() );
	}

	public static void assertEqualTriplePatternsVUV( final String expectedSubjectVarName,
	                                                 final String expectedPredicateURI,
	                                                 final String expectedObjectVarName,
	                                                 final TriplePattern actualTriplePattern ) {
		assertEqualTriplePatternsVUV( expectedSubjectVarName,
		                              expectedPredicateURI,
		                              expectedObjectVarName,
		                              actualTriplePattern.asJenaTriple() );
	}

	public static void assertEqualTriplePatternsVUV( final String expectedSubjectVarName,
	                                                 final String expectedPredicateURI,
	                                                 final String expectedObjectVarName,
	                                                 final Triple actualTriplePattern ) {
		assertTrue( actualTriplePattern.getSubject().isVariable() );
		assertTrue( actualTriplePattern.getPredicate().isURI() );
		assertTrue( actualTriplePattern.getObject().isVariable() );
		assertEquals( expectedSubjectVarName, actualTriplePattern.getSubject().getName() );
		assertEquals( expectedPredicateURI, actualTriplePattern.getPredicate().getURI() );
		assertEquals( expectedObjectVarName, actualTriplePattern.getObject().getName() );
	}

}
