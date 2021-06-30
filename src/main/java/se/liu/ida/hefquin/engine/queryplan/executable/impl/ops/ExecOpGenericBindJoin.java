package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public abstract class ExecOpGenericBindJoin<QueryType extends Query, MemberType extends FederationMember>
        implements UnaryExecutableOp
{
    protected final QueryType query;
    protected final MemberType fm;

    public ExecOpGenericBindJoin(final QueryType query, final MemberType fm ) {
        assert query != null;
        assert fm != null;

        this.query = query;
        this.fm = fm;
    }

    @Override
    public int preferredInputBlockSize() {
        // This algorithm can process a sequence of input solution mappings.
        // To find a trade-off between #request and dataRecv, the size block size can be optimized in query planning
        return 30;
    }

    @Override
    public void process(
            final IntermediateResultBlock input,
            final IntermediateResultElementSink sink,
            final ExecutionContext execCxt) throws ExecOpExecutionException
    {
        for ( final SolutionMapping fetchedSM : fetchSolutionMappings( input.getSolutionMappings(), execCxt) ) {
            for ( final SolutionMapping inputSM : input.getSolutionMappings() ) {
                if(SolutionMappingUtils.compatible(inputSM, fetchedSM)){
                    final SolutionMapping mergedSM = SolutionMappingUtils.merge(inputSM, fetchedSM);
                    sink.send(mergedSM);
                }
            }
        }
    }

    @Override
    public void concludeExecution(
            final IntermediateResultElementSink sink,
            final ExecutionContext execCxt )
    {
        // nothing to be done here
    }

    protected abstract Iterable<SolutionMapping> fetchSolutionMappings(
            final Iterable<SolutionMapping> solMaps,
            final ExecutionContext execCxt ) throws ExecOpExecutionException;
}
