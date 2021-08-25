package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.List;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public class PhysicalPlanFactory
{
	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplans become children of the root operator.
	 */
	public static PhysicalPlan createPlan( final LogicalOperator lop,
	                                       final PhysicalPlan... subplans ) {
		if (      lop instanceof NullaryLogicalOp ) return createPlan( (NullaryLogicalOp) lop );
		else if ( lop instanceof UnaryLogicalOp )   return createPlan( (UnaryLogicalOp) lop, subplans[0] );
		else if ( lop instanceof BinaryLogicalOp )  return createPlan( (BinaryLogicalOp) lop, subplans[0], subplans[1] );
		else if ( lop instanceof NaryLogicalOp )    return createPlan( (NaryLogicalOp) lop, subplans );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplans become children of the root operator.
	 */
	public static PhysicalPlan createPlan( final PhysicalOperator lop,
	                                       final PhysicalPlan... subplans ) {
		if (      lop instanceof NullaryPhysicalOp ) return createPlan( (NullaryPhysicalOp) lop );
		else if ( lop instanceof UnaryPhysicalOp )   return createPlan( (UnaryPhysicalOp) lop, subplans[0] );
		else if ( lop instanceof BinaryPhysicalOp )  return createPlan( (BinaryPhysicalOp) lop, subplans[0], subplans[1] );
		else if ( lop instanceof NaryPhysicalOp )    return createPlan( (NaryPhysicalOp) lop, subplans );
		else throw new UnsupportedOperationException("Unsupported type of physical operator: " + lop.getClass().getName() + ".");
	}


	// --------- plans with nullary root operators -----------

	/**
	 * Creates a plan with a request operator as root operator.
	 */
	public static <R extends DataRetrievalRequest, M extends FederationMember>
	PhysicalPlan createPlanWithRequest( final LogicalOpRequest<R,M> lop ) {
		final NullaryPhysicalOp pop = new PhysicalOpRequest<>(lop);
		return createPlan(pop);
	}

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}.
	 */
	public static PhysicalPlan createPlan( final NullaryLogicalOp rootOp ) {
		final NullaryPhysicalOp pop = LogicalToPhysicalOpConverter.convert(rootOp);
		return createPlan(pop);
	}

	/**
	 * Creates a physical plan with the given root operator.
	 */
	public static PhysicalPlan createPlan( final NullaryPhysicalOp rootOp ) {
		return new PhysicalPlanWithNullaryRootImpl(rootOp) {};
	}


	// --------- plans with unary root operators -----------

	/**
	 * Creates a plan with a bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoin( final LogicalOpTPAdd lop,
	                                                   final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpBindJoin(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinFILTER( final LogicalOpTPAdd lop,
	                                                         final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpBindJoinWithFILTER(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinUNION( final LogicalOpTPAdd lop,
	                                                        final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpBindJoinWithUNION(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinVALUES( final LogicalOpTPAdd lop,
	                                                         final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpBindJoinWithVALUES(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with an index nested loops join as root operator.
	 */
	public static PhysicalPlan createPlanWithIndexNLJ( final LogicalOpTPAdd lop,
	                                                   final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpIndexNestedLoopsJoin(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with an index nested loops join as root operator.
	 */
	public static PhysicalPlan createPlanWithIndexNLJ( final LogicalOpBGPAdd lop,
	                                                   final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpIndexNestedLoopsJoin(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplan becomes the single child of the root operator.
	 */
	public static PhysicalPlan createPlan( final UnaryLogicalOp rootOp,
	                                       final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = LogicalToPhysicalOpConverter.convert(rootOp);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a physical plan with the given root operator. The
	 * given subplan becomes the single child of the root operator.
	 */
	public static PhysicalPlan createPlan( final UnaryPhysicalOp rootOp,
	                                       final PhysicalPlan subplan ) {
		return new PhysicalPlanWithUnaryRootImpl(rootOp, subplan) {};
	}


	// --------- plans with binary root operators -----------

	/**
	 * Creates a plan with a binary union as root operator.
	 */
	public static PhysicalPlan createPlanWithUnion( final PhysicalPlan subplan1,
	                                                final PhysicalPlan subplan2 ) {
		return createPlanWithUnion( new LogicalOpUnion(), subplan1, subplan2 );
	}

	/**
	 * Creates a plan with a binary union as root operator.
	 */
	public static PhysicalPlan createPlanWithUnion( final LogicalOpUnion lop,
	                                                final PhysicalPlan subplan1,
	                                                final PhysicalPlan subplan2 ) {
		final BinaryPhysicalOp pop = new PhysicalOpBinaryUnion(lop);
		return createPlan(pop, subplan1, subplan2);
	}

	/**
	 * Creates a plan with a hash join as root operator.
	 */
	public static PhysicalPlan createPlanWithHashJoin( final PhysicalPlan subplan1,
	                                                   final PhysicalPlan subplan2 ) {
		return createPlanWithHashJoin( new LogicalOpJoin(), subplan1, subplan2 );
	}

	/**
	 * Creates a plan with a hash join as root operator.
	 */
	public static PhysicalPlan createPlanWithHashJoin( final LogicalOpJoin lop,
	                                                   final PhysicalPlan subplan1,
	                                                   final PhysicalPlan subplan2 ) {
		final BinaryPhysicalOp pop = new PhysicalOpHashJoin(lop);
		return createPlan(pop, subplan1, subplan2);
	}

	/**
	 * Creates a plan with a symmetric hash join as root operator.
	 */
	public static PhysicalPlan createPlanWithSymmetricHashJoin( final PhysicalPlan subplan1,
	                                                            final PhysicalPlan subplan2 ) {
		return createPlanWithSymmetricHashJoin( new LogicalOpJoin(), subplan1, subplan2 );
	}

	/**
	 * Creates a plan with a symmetric hash join as root operator.
	 */
	public static PhysicalPlan createPlanWithSymmetricHashJoin( final LogicalOpJoin lop,
	                                                            final PhysicalPlan subplan1,
	                                                            final PhysicalPlan subplan2 ) {
		final BinaryPhysicalOp pop = new PhysicalOpSymmetricHashJoin(lop);
		return createPlan(pop, subplan1, subplan2);
	}

	/**
	 * Creates a plan with a naive nested loops join as root operator.
	 */
	public static PhysicalPlan createPlanWithNaiveNLJ( final PhysicalPlan subplan1,
	                                                   final PhysicalPlan subplan2 ) {
		return createPlanWithNaiveNLJ( new LogicalOpJoin(), subplan1, subplan2 );
	}

	/**
	 * Creates a plan with a naive nested loops join as root operator.
	 */
	public static PhysicalPlan createPlanWithNaiveNLJ( final LogicalOpJoin lop,
	                                                   final PhysicalPlan subplan1,
	                                                   final PhysicalPlan subplan2 ) {
		final BinaryPhysicalOp pop = new PhysicalOpNaiveNestedLoopsJoin(lop);
		return createPlan(pop, subplan1, subplan2);
	}

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplans become children of the root operator.
	 */
	public static PhysicalPlan createPlan( final BinaryLogicalOp rootOp,
	                                       final PhysicalPlan subplan1,
	                                       final PhysicalPlan subplan2 ) {
		final BinaryPhysicalOp pop = LogicalToPhysicalOpConverter.convert(rootOp);
		return createPlan(pop, subplan1, subplan2);
	}

	/**
	 * Creates a physical plan with the given root operator. The
	 * given subplans become children of the root operator.
	 */
	public static PhysicalPlan createPlan( final BinaryPhysicalOp rootOp,
	                                       final PhysicalPlan subplan1,
	                                       final PhysicalPlan subplan2 ) {
		return new PhysicalPlanWithBinaryRootImpl(rootOp, subplan1, subplan2) {};
	}


	// --------- plans with n-ary root operators -----------

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplans become children of the root operator.
	 */
	public static PhysicalPlan createPlan( final NaryLogicalOp rootOp,
	                                       final PhysicalPlan... subplans ) {
		final NaryPhysicalOp pop = LogicalToPhysicalOpConverter.convert(rootOp);
		return createPlan(pop, subplans);
	}

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplans become children of the root operator.
	 */
	public static PhysicalPlan createPlan( final NaryLogicalOp rootOp,
	                                       final List<PhysicalPlan> subplans ) {
		final NaryPhysicalOp pop = LogicalToPhysicalOpConverter.convert(rootOp);
		return createPlan(pop, subplans);
	}

	/**
	 * Creates a physical plan with the given root operator. The
	 * given subplans become children of the root operator.
	 */
	public static PhysicalPlan createPlan( final NaryPhysicalOp rootOp,
	                                       final PhysicalPlan... subplans ) {
		return new PhysicalPlanWithNaryRootImpl(rootOp, subplans) {};
	}

	/**
	 * Creates a physical plan with the given root operator. The
	 * given subplans become children of the root operator.
	 */
	public static PhysicalPlan createPlan( final NaryPhysicalOp rootOp,
	                                       final List<PhysicalPlan> subplans ) {
		return new PhysicalPlanWithNaryRootImpl(rootOp, subplans) {};
	}

}
