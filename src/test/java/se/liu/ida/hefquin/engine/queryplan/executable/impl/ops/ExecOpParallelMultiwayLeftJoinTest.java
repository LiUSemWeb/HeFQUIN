package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Graph;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpParallelMultiwayLeftJoinTest extends TestsForTPAddAlgorithms<SPARQLEndpoint>
{
	protected static ExecutorService execService;

	@BeforeClass
	public static void createExecService() {
		execService = Executors.newCachedThreadPool();
	}

	@AfterClass
	public static void tearDownExecService() {
		execService.shutdownNow();
		try {
			execService.awaitTermination(500L, TimeUnit.MILLISECONDS);
		}
		catch ( final InterruptedException ex )  {
			System.err.println("Terminating the thread pool was interrupted." );
			ex.printStackTrace();
		}
	}


	@Test
	public void tpWithJoinOnObject() throws ExecutionException {
		_tpWithJoinOnObject(true);
	}

	@Test
	public void tpWithJoinOnSubjectAndObject() throws ExecutionException {
		_tpWithJoinOnSubjectAndObject(true);
	}

	@Test
	public void tpWithoutJoinVariable() throws ExecutionException {
		_tpWithoutJoinVariable(true);
	}

	@Test
	public void tpWithEmptyInput() throws ExecutionException {
		_tpWithEmptyInput(true);
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput() throws ExecutionException {
		_tpWithEmptySolutionMappingAsInput(true);
	}

	@Test
	public void tpWithEmptyResponses() throws ExecutionException {
		_tpWithEmptyResponses(true);
	}

	@Test
	public void tpWithIllegalBNodeJoin() throws ExecutionException {
		_tpWithIllegalBNodeJoin(true);
	}


	@Override
	protected SPARQLEndpoint createFedMemberForTest(Graph dataForMember) {
		return new SPARQLEndpointForTest(dataForMember);
	}

	@Override
	protected ExecutorService getExecutorServiceForTest() {
		return execService;
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest( final TriplePattern tp,
	                                                 final SPARQLEndpoint fm,
	                                                 final ExpectedVariables expectedVariables,
	                                                 final boolean useOuterJoinSemantics ) {
		if ( ! useOuterJoinSemantics )
			throw new UnsupportedOperationException();

		final TriplePatternRequest req = new TriplePatternRequestImpl(tp);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(fm, req);
		return new ExecOpParallelMultiwayLeftJoin( expectedVariables, reqOp );
	}


}
