package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.VocabularyMapping;

public class CollectingIntermediateResultElementSinkWithTranslation extends CollectingIntermediateResultElementSink
{
	protected final VocabularyMapping vm;
	protected final List<SolutionMapping> l = new ArrayList<>();

	public CollectingIntermediateResultElementSinkWithTranslation( final VocabularyMapping vm ) {
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

