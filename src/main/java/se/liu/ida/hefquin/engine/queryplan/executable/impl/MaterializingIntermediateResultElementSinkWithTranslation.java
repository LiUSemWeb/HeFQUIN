package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;

public class MaterializingIntermediateResultElementSinkWithTranslation extends MaterializingIntermediateResultElementSink
{
	protected final VocabularyMapping vm;
	protected final List<SolutionMapping> l = new ArrayList<>();

	public MaterializingIntermediateResultElementSinkWithTranslation( final VocabularyMapping vm ) {
		assert vm != null;
		this.vm = vm;
	}
	
	@Override
	public void send( final SolutionMapping element ) {
		for ( final SolutionMapping sm : vm.translateSolutionMapping(element) ) {
			super.send(sm);
		}
	}

}

