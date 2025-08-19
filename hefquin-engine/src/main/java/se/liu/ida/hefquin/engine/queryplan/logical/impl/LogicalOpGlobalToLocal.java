package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlanOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpGlobalToLocal extends BaseForQueryPlanOperator implements UnaryLogicalOp {

	protected final VocabularyMapping vocabularyMapping;
	
	public LogicalOpGlobalToLocal(final VocabularyMapping mapping){
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
		return "> g2l " + "(vocab.mapping: " + vocabularyMapping.hashCode() + ")";
	}

	public VocabularyMapping getVocabularyMapping() {
		return this.vocabularyMapping;
	}

}