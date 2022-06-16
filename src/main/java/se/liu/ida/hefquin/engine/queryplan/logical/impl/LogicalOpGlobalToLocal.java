package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpGlobalToLocal implements UnaryLogicalOp {

	protected final VocabularyMapping vocabularyMapping;
	
	public LogicalOpGlobalToLocal(final VocabularyMapping mapping){
		this.vocabularyMapping = mapping;
	}
	
	@Override
	public void visit(final LogicalPlanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "> g2l " + "(vocab.mapping: " + vocabularyMapping.hashCode() + ")";
	}

	public VocabularyMapping getVocabularyMapping() {
		return this.vocabularyMapping;
	}

}
