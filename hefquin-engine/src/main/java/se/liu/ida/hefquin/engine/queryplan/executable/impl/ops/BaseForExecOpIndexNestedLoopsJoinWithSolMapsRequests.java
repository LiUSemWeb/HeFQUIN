package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

public abstract class BaseForExecOpIndexNestedLoopsJoinWithSolMapsRequests<QueryType extends Query,
                                                                           MemberType extends FederationMember,
                                                                           ReqType extends DataRetrievalRequest>
     extends BaseForExecOpIndexNestedLoopsJoinWithRequests<QueryType,MemberType,ReqType,SolMapsResponse>
{
	private static final Logger log = LoggerFactory.getLogger( BaseForExecOpIndexNestedLoopsJoinWithSolMapsRequests.class );

	public BaseForExecOpIndexNestedLoopsJoinWithSolMapsRequests( final QueryType query,
	                                                             final MemberType fm,
	                                                             final boolean mayReduce,
	                                                             final int minimumInputBlockSize,
	                                                             final boolean collectExceptions,
	                                                             final QueryPlanningInfo qpInfo ) {
		super(query, fm, mayReduce, minimumInputBlockSize, collectExceptions, qpInfo);
	}

	@Override
	protected MyResponseProcessor createResponseProcessor( final SolutionMapping sm,
	                                                       final IntermediateResultElementSink sink,
	                                                       final ExecutableOperator op )
	{
		log.info( "Creating response processor for solution mapping." );
		return new MyResponseProcessor( sm, sink, op ) {
			@Override
			protected Iterable<SolutionMapping> extractSolMaps( final SolMapsResponse response )
					throws UnsupportedOperationDueToRetrievalError {
				log.info( "Extracting solution mappings from federation response." );
				return response.getResponseData();
			}

			@Override
			protected void processExtractedSolMaps( final Iterable<SolutionMapping> solmaps ) {
				log.info( "Merging solution mappings with input mapping." );
				for ( final SolutionMapping fetchedSM : solmaps ) {
					final SolutionMapping out = SolutionMappingUtils.merge( sm, fetchedSM );
					sink.send( out );
				}
			}
		};
	}

}
