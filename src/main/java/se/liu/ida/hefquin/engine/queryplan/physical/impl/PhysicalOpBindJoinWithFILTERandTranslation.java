package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithFILTERandTranslation;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

public class PhysicalOpBindJoinWithFILTERandTranslation extends PhysicalOpBindJoinWithFILTER
{
	public PhysicalOpBindJoinWithFILTERandTranslation( final LogicalOpTPAdd lop ) {
		super(lop);

		assert lop.getFederationMember().getVocabularyMapping() != null;
	}

	public PhysicalOpBindJoinWithFILTERandTranslation( final LogicalOpTPOptAdd lop ) {
		super(lop);

		assert lop.getFederationMember().getVocabularyMapping() != null;
	}

	public PhysicalOpBindJoinWithFILTERandTranslation( final LogicalOpBGPAdd lop ) {
		super(lop);

		assert lop.getFederationMember().getVocabularyMapping() != null;
	}

	public PhysicalOpBindJoinWithFILTERandTranslation( final LogicalOpBGPOptAdd lop ) {
		super(lop);

		assert lop.getFederationMember().getVocabularyMapping() != null;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoinWithFILTERandTranslation
				&& ((PhysicalOpBindJoinWithFILTERandTranslation) o).lop.equals(lop);
	}

	@Override
	protected UnaryExecutableOp createExecOp( final SPARQLGraphPattern pattern,
	                                          final FederationMember fm,
	                                          final boolean useOuterJoinSemantics ) {
		if ( fm instanceof SPARQLEndpoint )
			return new ExecOpBindJoinSPARQLwithFILTERandTranslation( pattern, (SPARQLEndpoint) fm, useOuterJoinSemantics );
		else
			throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
	}

	@Override
	public void visit(final PhysicalPlanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {

		return "> FILTERBindJoinWithTranslation" +  lop.toString();
	}

}
