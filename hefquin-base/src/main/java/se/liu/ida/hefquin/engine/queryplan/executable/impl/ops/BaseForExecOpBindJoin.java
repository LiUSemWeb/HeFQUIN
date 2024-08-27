package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * An abstract base class for the different variants to implement the bind join algorithm.
 *
 * All that is needed for concrete classes that extend this base class is to implement the
 * {@link UnaryExecutableOpBase#_process(se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock, IntermediateResultElementSink, ExecutionContext)}
 * function.
 */
public abstract class BaseForExecOpBindJoin<QueryType extends Query,
                                            MemberType extends FederationMember>
        extends UnaryExecutableOpBase
{
	public static final int defaultPreferredInputBlockSize = 30;

    protected final QueryType query;
    protected final MemberType fm;

    public BaseForExecOpBindJoin( final QueryType query, final MemberType fm, final boolean collectExceptions ) {
        super(collectExceptions);
        assert query != null;
        assert fm != null;

        this.query = query;
        this.fm = fm;
    }

    @Override
    public int preferredInputBlockSize() {
        // This algorithm can process a sequence of input solution mappings.
        // To find a trade-off between #request and dataRecv, the block size can be optimized in query planning
        return defaultPreferredInputBlockSize;
    }

    @Override
    protected void _concludeExecution(
            final IntermediateResultElementSink sink,
            final ExecutionContext execCxt )
    {
        // nothing to be done here
    }

    protected ExecutableOperatorStatsImpl createStats() {
        final ExecutableOperatorStatsImpl s = super.createStats();
        s.put( "queryAsString",      query.toString() );
        s.put( "fedMemberAsString",  fm.toString() );
        return s;
    }
}
