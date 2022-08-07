package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Set;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.data.utils.RewritingIteratorForSolMapsG2L;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpBindJoinSPARQLwithFILTERandTranslation extends ExecOpBindJoinSPARQLwithFILTER{
	
	public ExecOpBindJoinSPARQLwithFILTERandTranslation( final SPARQLGraphPattern query,
	                                                     final SPARQLEndpoint fm,
	                                                     final boolean useOuterJoinSemantics,
	                                                     final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions);

		assert fm.getVocabularyMapping() != null;
	}

	@Override
	protected void _process( final Iterable<SolutionMapping> joinableInputSMs,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt)
			throws ExecOpExecutionException
	{
		final VocabularyMapping vm = fm.getVocabularyMapping();
		final Iterable<SolutionMapping> translatedSMs = RewritingIteratorForSolMapsG2L.createAsIterable(joinableInputSMs, vm);
		final IntermediateResultElementSink mySink = new MyIntermediateResultElementSinkWithL2GTranslation(sink, vm);

		super._process(translatedSMs, mySink, execCxt);
	}


	protected static class MyIntermediateResultElementSinkWithL2GTranslation implements IntermediateResultElementSink
	{
		protected final IntermediateResultElementSink outputSink;
		protected final VocabularyMapping vm;

		public MyIntermediateResultElementSinkWithL2GTranslation( final IntermediateResultElementSink outputSink,
		                                                          final VocabularyMapping vm ) {
			this.outputSink = outputSink;
			this.vm = vm;
		}

		@Override
		public void send( final SolutionMapping sm ) {
			final Set<SolutionMapping> translatedSolMaps = vm.translateSolutionMapping(sm);
			for ( final SolutionMapping translatedSM : translatedSolMaps ) {
				outputSink.send(translatedSM);
			}
		}
	}

}
