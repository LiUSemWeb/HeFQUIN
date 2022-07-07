package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;

/**
 * Base class for implementations of request operators.
 */
public abstract class ExecOpGenericRequest<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
                extends NullaryExecutableOpBase
{
	protected final ReqType req;
	protected final MemberType fm;

	public ExecOpGenericRequest( final ReqType req, final MemberType fm ) {
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
