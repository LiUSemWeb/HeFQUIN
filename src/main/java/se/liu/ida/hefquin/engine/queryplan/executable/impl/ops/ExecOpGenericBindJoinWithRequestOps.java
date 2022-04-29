package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.core.BasicPattern;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
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
public abstract class ExecOpGenericBindJoinWithRequestOps<QueryType extends Query,
                                                          MemberType extends FederationMember>
           extends ExecOpGenericBindJoinBase<QueryType,MemberType>
{
	public ExecOpGenericBindJoinWithRequestOps( final QueryType query, final MemberType fm ) {
		super(query, fm);
	}

	@Override
	protected void _process( final IntermediateResultBlock input,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt)
			throws ExecOpExecutionException
	{
		final NullaryExecutableOp reqOp = createExecutableRequestOperator( input.getSolutionMappings() );
		if ( reqOp != null ) {
			final IntermediateResultElementSink mySink = new MyIntermediateResultElementSink(sink, input);
			try {
				reqOp.execute(mySink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
			}
		}
	}

	protected abstract NullaryExecutableOp createExecutableRequestOperator( Iterable<SolutionMapping> solMaps );


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
    } // end of helper class MyIntermediateResultElementSink
	
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
			final Set<SolutionMapping> smFromRequestTranslated = vocabularyMapping.translateSolutionMapping(smFromRequest, true);
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				for (final SolutionMapping smTranslated : smFromRequestTranslated) {
					if ( SolutionMappingUtils.compatible(smFromInput, smTranslated) ) {
						outputSink.send( SolutionMappingUtils.merge(smFromInput,smTranslated) );
					}
				}
			}
		}
    }

	// ------- helper function ------
	/**
	 * Returns a representation of this query pattern as an
	 * object of the interface {@link Op} of the Jena API.
	 */
	protected Op representQueryPatternAsJenaOp( final QueryType query ) {
		if ( query instanceof SPARQLGraphPattern ) {
			if ( query instanceof TriplePattern) {
				return new OpTriple( ((TriplePattern)query).asJenaTriple());
			}
			else if (query instanceof BGP) {
				final BasicPattern bgp = new BasicPattern();
				for ( final TriplePattern tp : ((BGP) query).getTriplePatterns() ) {
					bgp.add( tp.asJenaTriple() );
				}
				return new OpBGP(bgp);
			}
			else if ( query instanceof GenericSPARQLGraphPatternImpl1 ) {
				@SuppressWarnings("deprecation")
				final Op jenaOp = ( (GenericSPARQLGraphPatternImpl1) query ).asJenaOp();
				return jenaOp;
			}
			else if ( query instanceof GenericSPARQLGraphPatternImpl2 ) {
				return ( (GenericSPARQLGraphPatternImpl2) query ).asJenaOp();
			}
			else {
				throw new UnsupportedOperationException( query.getClass().getName() );
			}
		}
		else
			throw new IllegalArgumentException("Unsupported type of query pattern: " + query.getClass().getName() );
	}

}
