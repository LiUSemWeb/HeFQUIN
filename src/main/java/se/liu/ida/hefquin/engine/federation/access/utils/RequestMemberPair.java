package se.liu.ida.hefquin.engine.federation.access.utils;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.utils.Pair;

public class RequestMemberPair extends Pair<DataRetrievalRequest,FederationMember>
{
	public RequestMemberPair( final DataRetrievalRequest object1,
	                          final FederationMember object2 ) {
		super(object1, object2);
	}

	public RequestMemberPair( final LogicalOpRequest<?,?> reqOp ) {
		this( reqOp.getRequest(), reqOp.getFederationMember() );
	}

	public RequestMemberPair( final PhysicalOpRequest<?,?> reqOp ) {
		this( reqOp.getLogicalOperator() );
	}

	public DataRetrievalRequest getRequest() { return object1; }
	public FederationMember getMember() { return object2; }
}
