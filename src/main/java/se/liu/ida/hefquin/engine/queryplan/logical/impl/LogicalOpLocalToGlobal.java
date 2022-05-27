package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpLocalToGlobal implements UnaryLogicalOp {

	protected final VocabularyMapping vocabularyMapping;
	
	public LogicalOpLocalToGlobal(final VocabularyMapping mapping){
		this.vocabularyMapping = mapping;
	}
	
	@Override
	public void visit(final LogicalPlanVisitor visitor) {
		visitor.visit(this);
	}
	
	public VocabularyMapping getVocabularyMapping() {
		return this.vocabularyMapping;
	}

}
