package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpLocalToGlobal implements UnaryLogicalOp
{
	protected final VocabularyMapping vm;

	public LogicalOpLocalToGlobal( final VocabularyMapping vm ){
		this.vm = vm;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		return inputVars[0];
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	public VocabularyMapping getVocabularyMapping() {
		return vm;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return o instanceof LogicalOpLocalToGlobal oo && oo.vm.equals(vm);
	}

	@Override
	public int hashCode(){
		return getClass().hashCode() ^ vm.hashCode();
	}

	@Override
	public String toString() {
		return "l2g (" + vm.hashCode() + ")";
	}

}
