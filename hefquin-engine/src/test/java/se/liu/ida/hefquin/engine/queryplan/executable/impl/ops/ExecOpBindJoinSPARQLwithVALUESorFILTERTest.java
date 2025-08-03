package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.concurrent.ExecutorService;

import org.apache.jena.graph.Graph;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;

public class ExecOpBindJoinSPARQLwithVALUESorFILTERTest extends TestsForTPAddAlgorithms<SPARQLEndpoint>
{
	protected boolean forceFailureForVALUES = false;

	@Test
	public void tpWithJoinOnObject_InnerJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithJoinOnObject(false);
	}

	@Test
	public void tpWithJoinOnObject_OuterJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithJoinOnObject(true);
	}

	@Test
	public void tpWithJoinOnSubjectAndObject_InnerJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithJoinOnSubjectAndObject(false);
	}

	@Test
	public void tpWithJoinOnSubjectAndObject_OuterJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithJoinOnSubjectAndObject(true);
	}

	@Test
	public void tpWithoutJoinVariable_InnerJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithoutJoinVariable(false);
	}

	@Test
	public void tpWithoutJoinVariable_OuterJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithoutJoinVariable(true);
	}

	@Test
	public void tpWithEmptyInput_InnerJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithEmptyInput(false);
	}

	@Test
	public void tpWithEmptyInput_OuterJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithEmptyInput(true);
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput_InnerJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithEmptySolutionMappingAsInput(false);
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput_OuterJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithEmptySolutionMappingAsInput(true);
	}

	@Test
	public void tpWithEmptyResponses_InnerJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithEmptyResponses(false);
	}

	@Test
	public void tpWithEmptyResponses_OuterJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithEmptyResponses(true);
	}

	@Test
	public void tpWithIllegalBNodeJoin_InnerJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithIllegalBNodeJoin(false);
	}

	@Test
	public void tpWithIllegalBNodeJoin_OuterJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithIllegalBNodeJoin(true);
	}

	@Test
	public void tpWithSpuriousDuplicates_InnerJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithSpuriousDuplicates(false);
	}

	@Test
	public void tpWithSpuriousDuplicates_OuterJoin_WithVALUES() throws ExecutionException {
		forceFailureForVALUES = false;
		_tpWithSpuriousDuplicates(true);
	}

	@Test
	public void tpWithJoinOnObject_InnerJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithJoinOnObject(false);
	}

	@Test
	public void tpWithJoinOnObject_OuterJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithJoinOnObject(true);
	}

	@Test
	public void tpWithJoinOnSubjectAndObject_InnerJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithJoinOnSubjectAndObject(false);
	}

	@Test
	public void tpWithJoinOnSubjectAndObject_OuterJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithJoinOnSubjectAndObject(true);
	}

	@Test
	public void tpWithoutJoinVariable_InnerJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithoutJoinVariable(false);
	}

	@Test
	public void tpWithoutJoinVariable_OuterJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithoutJoinVariable(true);
	}

	@Test
	public void tpWithEmptyInput_InnerJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithEmptyInput(false);
	}

	@Test
	public void tpWithEmptyInput_OuterJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithEmptyInput(true);
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput_InnerJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithEmptySolutionMappingAsInput(false);
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput_OuterJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithEmptySolutionMappingAsInput(true);
	}

	@Test
	public void tpWithEmptyResponses_InnerJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithEmptyResponses(false);
	}

	@Test
	public void tpWithEmptyResponses_OuterJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithEmptyResponses(true);
	}

	@Test
	public void tpWithIllegalBNodeJoin_InnerJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithIllegalBNodeJoin(false);
	}

	@Test
	public void tpWithIllegalBNodeJoin_OuterJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithIllegalBNodeJoin(true);
	}

	@Test
	public void tpWithSpuriousDuplicates_InnerJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithSpuriousDuplicates(false);
	}

	@Test
	public void tpWithSpuriousDuplicates_OuterJoin_WithFILTER() throws ExecutionException {
		forceFailureForVALUES = true;
		_tpWithSpuriousDuplicates(true);
	}



	@Override
	protected SPARQLEndpoint createFedMemberForTest( final Graph dataForMember ) {
		return new SPARQLEndpointForTest(dataForMember) {
			@Override
			public SolMapsResponse performRequest( final SPARQLRequest req )
					throws FederationAccessException {
				if ( forceFailureForVALUES &&
				     req.getQueryPattern().toStringForPlanPrinters().contains("VALUES") )
					throw new FederationAccessException("Test endpoint that pretends it cannot do VALUES clauses.", req, this);
				else
					return super.performRequest(req);
			}
		};
	}

	@Override
	protected ExecutorService getExecutorServiceForTest() {
		return null;
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest( final TriplePattern tp,
	                                                 final SPARQLEndpoint fm,
	                                                 final ExpectedVariables expectedVariables,
	                                                 final boolean useOuterJoinSemantics ) {

		return new ExecOpBindJoinSPARQLwithVALUESorFILTER( tp,
		                                                   fm,
		                                                   expectedVariables,
		                                                   useOuterJoinSemantics,
		                                                   ExecOpBindJoinSPARQLwithVALUESorFILTER.DEFAULT_BATCH_SIZE,
		                                                   false,
		                                                   null );
	}

}
