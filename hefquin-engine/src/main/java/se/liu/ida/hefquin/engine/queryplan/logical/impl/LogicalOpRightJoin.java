package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpRightJoin extends LogicalOperatorBase implements BinaryLogicalOp
{
	protected static LogicalOpRightJoin singleton = new LogicalOpRightJoin();

	public static LogicalOpRightJoin getInstance() { return singleton; }

	protected LogicalOpRightJoin() {}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

		final Set<Var> certainVars = inputVars[1].getCertainVariables();

		final Set<Var> possibleVars = new HashSet<>();
		possibleVars.addAll( inputVars[0].getCertainVariables() );
		possibleVars.addAll( inputVars[0].getPossibleVariables() );
		possibleVars.addAll( inputVars[1].getPossibleVariables() );
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpRightJoin; 
	}

	@Override
	public int hashCode(){
		return Objects.hash( this.getClass().getName() );
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		return "> leftjoin ";
	}

}
