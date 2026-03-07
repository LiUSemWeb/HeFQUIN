package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;

/**
 * Base class for implementations of request operators.
 */
public abstract class BaseForExecOpRequest<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
                extends NullaryExecutableOpBase
{
	protected final ReqType req;
	protected final MemberType fm;

	public BaseForExecOpRequest( final ReqType req,
	                             final MemberType fm,
	                             final boolean collectExceptions,
	                             final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);

		assert req != null;
		assert fm != null;

		this.req = req;
		this.fm = fm;
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "requestAsString",    req.toString() );
		s.put( "fedMemberAsString",  fm.toString() );
		return s;
	}

}
