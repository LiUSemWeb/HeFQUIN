package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.RewritingIterableForSolMapsG2L;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpBindJoinSPARQLwithFILTERandTranslation extends ExecOpBindJoinSPARQLwithFILTER{
	
	public ExecOpBindJoinSPARQLwithFILTERandTranslation( final SPARQLGraphPattern query, final SPARQLEndpoint fm ) {
		super(query, fm);

		assert fm.getVocabularyMapping() != null;
	}

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final Iterable<SolutionMapping> solMaps ) {
		final Iterable<SolutionMapping> translatedSolMaps = new RewritingIterableForSolMapsG2L( solMaps, fm.getVocabularyMapping() );
		return super.createExecutableRequestOperator(translatedSolMaps);
	}	

	@Override
	protected void _process( final IntermediateResultBlock input,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt)
			throws ExecOpExecutionException
	{
		final NullaryExecutableOp reqOp = createExecutableRequestOperator( input.getSolutionMappings() );
		if ( reqOp != null ) {
			final IntermediateResultElementSink mySink = new MyIntermediateResultElementSinkWithTranslation(sink, input, fm.getVocabularyMapping());
			try {
				reqOp.execute(mySink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
			}
		}
	}
	
}
