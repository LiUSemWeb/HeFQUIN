package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Abstract base class to implement bind joins by using request operators.
 *
 * Note that executing the request operator is a blocking operation within
 * the algorithm implemented by this class. However, it does not matter
 * because this bind join algorithm uses only one request for any given
 * {@link IntermediateResultBlock}. Issuing the request directly (and then
 * using a response processor) would also be blocking because we would have
 * to wait for the response processor. Attention: things may look different
 * if we have to do multiple requests per {@link IntermediateResultBlock},
 * which may be the case if the block size is greater than what the
 * server can/wants to handle.
 */
public abstract class BaseForExecOpBindJoinWithRequestOps<QueryType extends Query,
                                                          MemberType extends FederationMember>
           extends BaseForExecOpBindJoin<QueryType,MemberType>
{
	protected final boolean useOuterJoinSemantics;

	public BaseForExecOpBindJoinWithRequestOps( final QueryType query,
	                                            final MemberType fm,
	                                            final boolean useOuterJoinSemantics ) {
		super(query, fm);
		this.useOuterJoinSemantics = useOuterJoinSemantics;
	}

	@Override
	protected void _process( final IntermediateResultBlock input,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt)
			throws ExecOpExecutionException
	{
		final List<SolutionMapping> unjoinableInputSMs;
		if ( useOuterJoinSemantics )
			unjoinableInputSMs = new ArrayList<>();
		else
			unjoinableInputSMs = null;

		final NullaryExecutableOp reqOp = createExecutableRequestOperator( input.getSolutionMappings(), unjoinableInputSMs );

		if ( useOuterJoinSemantics ) {
			for ( final SolutionMapping sm : unjoinableInputSMs ) {
				sink.send(sm);
			}
		}

		if ( reqOp != null ) {
			final MyIntermediateResultElementSink mySink;
			if ( useOuterJoinSemantics )
				mySink = new MyIntermediateResultElementSinkOuterJoin(sink, input);
			else
				mySink = new MyIntermediateResultElementSink(sink, input);

			try {
				reqOp.execute(mySink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
			}

			mySink.flush();
		}
	}

	protected abstract NullaryExecutableOp createExecutableRequestOperator( Iterable<SolutionMapping> solMaps,
	                                                                        List<SolutionMapping> unjoinableInputSMs );


	// ------- helper classes ------

	protected static class MyIntermediateResultElementSink implements IntermediateResultElementSink
	{
		protected final IntermediateResultElementSink outputSink;
		protected final Iterable<SolutionMapping> inputSolutionMappings;

		public MyIntermediateResultElementSink( final IntermediateResultElementSink outputSink,
		                                        final IntermediateResultBlock input ) {
			this.outputSink = outputSink;
			this.inputSolutionMappings = input.getSolutionMappings();
		}

		@Override
		public void send( final SolutionMapping smFromRequest ) {
			// TODO: this implementation is very inefficient
			// We need an implementation of IntermediateResultBlock that can
			// be used like an index.
			// See: https://github.com/LiUSemWeb/HeFQUIN/issues/3
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( SolutionMappingUtils.compatible(smFromInput, smFromRequest) ) {
					outputSink.send( SolutionMappingUtils.merge(smFromInput,smFromRequest) );
				}
			}
		}

		public void flush() { }

	} // end of helper class MyIntermediateResultElementSink


	protected static class MyIntermediateResultElementSinkOuterJoin extends MyIntermediateResultElementSink
	{
		protected final Set<SolutionMapping> inputSolutionMappingsWithJoinPartners = new HashSet<>();

		public MyIntermediateResultElementSinkOuterJoin( final IntermediateResultElementSink outputSink,
		                                                 final IntermediateResultBlock input ) {
			super(outputSink, input);
		}

		@Override
		public void send( final SolutionMapping smFromRequest ) {
			// TODO: this implementation is very inefficient
			// We need an implementation of IntermediateResultBlock that can
			// be used like an index.
			// See: https://github.com/LiUSemWeb/HeFQUIN/issues/3
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( SolutionMappingUtils.compatible(smFromInput, smFromRequest) ) {
					outputSink.send( SolutionMappingUtils.merge(smFromInput,smFromRequest) );
					inputSolutionMappingsWithJoinPartners.add(smFromInput);
				}
			}
		}

		/**
		 * Sends to the output sink all input solution
		 * mappings that did not have a join partner.
		 */
		@Override
		public void flush() {
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( ! inputSolutionMappingsWithJoinPartners.contains(smFromInput) ) {
					outputSink.send(smFromInput);
				}
			}
		}

	} // end of helper class MyIntermediateResultElementSinkOuterJoin


	protected static class MyIntermediateResultElementSinkWithTranslation implements IntermediateResultElementSink
	{
		protected final IntermediateResultElementSink outputSink;
		protected final Iterable<SolutionMapping> inputSolutionMappings;
		protected final VocabularyMapping vocabularyMapping;

		public MyIntermediateResultElementSinkWithTranslation( final IntermediateResultElementSink outputSink,
		                                        final IntermediateResultBlock input,
		                                        final VocabularyMapping vm) {
			this.outputSink = outputSink;
			this.inputSolutionMappings = input.getSolutionMappings();
			this.vocabularyMapping = vm;
		}

		@Override
		public void send( final SolutionMapping smFromRequest ) {
			// TODO: this implementation is very inefficient
			// We need an implementation of IntermediateResultBlock that can
			// be used like an index.
			// See: https://github.com/LiUSemWeb/HeFQUIN/issues/3
			final Set<SolutionMapping> smFromRequestTranslated = vocabularyMapping.translateSolutionMapping(smFromRequest);
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				for (final SolutionMapping smTranslated : smFromRequestTranslated) {
					if ( SolutionMappingUtils.compatible(smFromInput, smTranslated) ) {
						outputSink.send( SolutionMappingUtils.merge(smFromInput,smTranslated) );
					}
				}
			}
		}
    }

}
