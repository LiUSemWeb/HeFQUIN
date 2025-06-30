package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

public abstract class BaseForExecOpIndexNestedLoopsJoinWithSolMapsRequests<QueryType extends Query,
                                                                           MemberType extends FederationMember,
                                                                           ReqType extends DataRetrievalRequest>
     extends BaseForExecOpIndexNestedLoopsJoinWithRequests<QueryType,MemberType,ReqType,SolMapsResponse>
{
	public BaseForExecOpIndexNestedLoopsJoinWithSolMapsRequests( final QueryType query,
	                                                             final MemberType fm,
	                                                             final boolean collectExceptions ) {
		super(query, fm, collectExceptions);
	}

	@Override
	protected MyResponseProcessor createResponseProcessor( final SolutionMapping sm,
	                                                       final IntermediateResultElementSink sink,
	                                                       final ExecutableOperator op )
	{
		return new MyResponseProcessor( sm, sink, op ) {
			@Override
			protected Iterable<SolutionMapping> extractSolMaps( final SolMapsResponse response )
					throws UnsupportedOperationDueToRetrievalError {
				return response.getResponseData();
			}
		};
	}

}
