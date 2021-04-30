package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;

/**
 * Base class for implementations of request operators.
 */
public abstract class ExecOpGenericRequest<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
                implements NullaryExecutableOp
{
	protected final ReqType req;
	protected final MemberType fm;

	public ExecOpGenericRequest( final ReqType req, final MemberType fm ) {
		assert req != null;
		assert fm != null;

		this.req = req;
		this.fm = fm;
	}

	@Override
	public int preferredInputBlockSize() {
		return 0; // irrelevant because operators of this type do not have any input
	}

}
