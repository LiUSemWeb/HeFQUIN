package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpLocalToGlobal extends LogicalOperatorBase implements UnaryLogicalOp {
	
	protected final VocabularyMapping vocabularyMapping;
	
	public LogicalOpLocalToGlobal(final VocabularyMapping mapping){
		this.vocabularyMapping = mapping;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		return inputVars[0];
	}

	@Override
	public void visit(final LogicalPlanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "> l2g " + "(vocab.mapping: " + vocabularyMapping.hashCode() + ")";
	}

	public VocabularyMapping getVocabularyMapping() {
		return this.vocabularyMapping;
	}

}
