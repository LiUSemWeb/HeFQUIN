package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;

public class MaterializingIntermediateResultElementSinkWithTranslation extends MaterializingIntermediateResultElementSink {
	
	protected final VocabularyMapping vocabularyMapping;
	protected final List<SolutionMapping> l = new ArrayList<>();
	private boolean closed = false;
	
	public MaterializingIntermediateResultElementSinkWithTranslation(final VocabularyMapping vm) {
		this.vocabularyMapping = vm;
	}
	
	@Override
	public void send(SolutionMapping element) {
		if ( ! closed ) {
			for (SolutionMapping sm : vocabularyMapping.translateSolutionMapping(element)) {
				l.add(sm);
			}
		}
	}

}

