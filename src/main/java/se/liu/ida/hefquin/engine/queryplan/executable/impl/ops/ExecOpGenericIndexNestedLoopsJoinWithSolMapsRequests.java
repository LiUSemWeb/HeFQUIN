package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;

public abstract class ExecOpGenericIndexNestedLoopsJoinWithSolMapsRequests<QueryType extends Query,
                                                                           MemberType extends FederationMember,
                                                                           ReqType extends DataRetrievalRequest>
     extends ExecOpGenericIndexNestedLoopsJoinWithRequests<QueryType,MemberType,ReqType,SolMapsResponse>
{
	public ExecOpGenericIndexNestedLoopsJoinWithSolMapsRequests( final QueryType query, final MemberType fm ) {
		super( query, fm );
	}

	@Override
	protected MyResponseProcessor createResponseProcessor( final SolutionMapping sm, final IntermediateResultElementSink sink )
	{
		return new MyResponseProcessor(sm, sink) {
			@Override
			protected Iterable<SolutionMapping> extractSolMaps(final SolMapsResponse response) {
				return response.getSolutionMappings();
			}
		};
	}

}
