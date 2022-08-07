package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * An abstract base class for implementations of the (external) index nested loops join algorithm.
 */
public abstract class BaseForExecOpIndexNestedLoopsJoin<QueryType extends Query,
                                                            MemberType extends FederationMember>
                   extends UnaryExecutableOpBase
{
	protected final QueryType query;
	protected final MemberType fm;

	public BaseForExecOpIndexNestedLoopsJoin( final QueryType query, final MemberType fm, final boolean collectExceptions ) {
		super(collectExceptions);

		assert query != null;
		assert fm != null;

		this.query = query;
		this.fm = fm;
	}

	@Override
	protected void _concludeExecution(
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt )
	{
		// nothing to be done here
	}

}
