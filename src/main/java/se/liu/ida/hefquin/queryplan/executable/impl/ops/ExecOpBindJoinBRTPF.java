package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMappingUtils;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.query.TriplePattern;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedQueryPatternUtils;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.access.impl.req.BindingsRestrictedTriplePatternRequestImpl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class ExecOpBindJoinBRTPF implements UnaryExecutableOp
{
    protected final TriplePattern tp;
    protected final BRTPFServer fm;
    protected final Set<Var> varsInTP;

    public ExecOpBindJoinBRTPF( final TriplePattern tp, final BRTPFServer fm ) {
        assert tp != null;
        assert fm != null;

        this.tp = tp;
        this.fm = fm;

        varsInTP = JenaBasedQueryPatternUtils.getVariablesInPattern(tp);
    }

	@Override
	public int preferredInputBlockSize() {
		// TODO the preferred input block size should depend on the brTPF server
		// See: https://github.com/LiUSemWeb/HeFQUIN/issues/2
		return 30;
	}

	@Override
	public void process(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt )
	{
		final BindingsRestrictedTriplePatternRequest req = createRequest(input);
		final ExecOpRequestBRTPF reqOp = new ExecOpRequestBRTPF(req, fm);
		reqOp.execute( new MyIntermediateResultElementSink(input,sink), execCxt );
	}

	@Override
	public void concludeExecution(
            final IntermediateResultElementSink sink,
            final ExecutionContext execCxt )
    {
        // nothing to be done here
    }

	protected BindingsRestrictedTriplePatternRequest createRequest( final IntermediateResultBlock input ) {
		final Set<SolutionMapping> restrictedSolMaps = new HashSet<>();
		final Iterator<SolutionMapping> it = input.iterator();
		while ( it.hasNext() ) {
			final JenaBasedSolutionMapping sm = (JenaBasedSolutionMapping) it.next();
			restrictedSolMaps.add( JenaBasedSolutionMappingUtils.restrict(sm, varsInTP) );
		}
		return new BindingsRestrictedTriplePatternRequestImpl( tp, restrictedSolMaps );
	}

	protected static class MyIntermediateResultElementSink implements IntermediateResultElementSink
	{
		protected final IntermediateResultBlock input;
		protected final IntermediateResultElementSink outputSink;

		public MyIntermediateResultElementSink( final IntermediateResultBlock input, final IntermediateResultElementSink outputSink ) {
			this.input = input;
			this.outputSink = outputSink;
		}

		@Override
		public void send( final SolutionMapping solMapFromRequest ) {
			// TODO: this implementation is very inefficient
			// We need an implementation of IntermediateResultBlock that can
			// be used like an index.
			// See: https://github.com/LiUSemWeb/HeFQUIN/issues/3
			final JenaBasedSolutionMapping smFromRequest = (JenaBasedSolutionMapping) solMapFromRequest;
			final Iterator<SolutionMapping> it = input.iterator();
			while ( it.hasNext() ) {
				final JenaBasedSolutionMapping smFromInput = (JenaBasedSolutionMapping) it.next();
				if ( JenaBasedSolutionMappingUtils.compatible(smFromRequest,smFromInput) ) {
					outputSink.send( JenaBasedSolutionMappingUtils.merge(smFromRequest,smFromInput) );
				}
			}
		}
	} // end of MyIntermediateResultElementSink

}
