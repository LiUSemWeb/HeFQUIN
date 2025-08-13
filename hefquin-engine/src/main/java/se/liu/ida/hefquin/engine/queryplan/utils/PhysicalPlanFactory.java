package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TPFRequestImpl;

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
		return createPlan( lop, null, subplans );
	}

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplans become children of the root operator.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the plan is indeed meant to be associated with it (for instance,
	 * when creating a physical plan for a logical plan that is associated
	 * with this object). Also, do not create different physical plans with
	 * the same {@link QueryPlanningInfo} object because these objects may
	 * later be extended with additional properties for each plan; instead,
	 * make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlan( final LogicalOperator lop,
	                                       final QueryPlanningInfo qpInfo,
	                                       final PhysicalPlan... subplans ) {
		if (      lop instanceof NullaryLogicalOp nullaryLOP ) return createPlan(nullaryLOP, qpInfo);
		else if ( lop instanceof UnaryLogicalOp unaryLOP )     return createPlan(unaryLOP, qpInfo, subplans[0]);
		else if ( lop instanceof BinaryLogicalOp binaryLOP )   return createPlan(binaryLOP, qpInfo, subplans[0], subplans[1]);
		else if ( lop instanceof NaryLogicalOp naryLOP )       return createPlan(naryLOP, qpInfo, subplans);
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplans become children of the root operator.
	 */
	public static PhysicalPlan createPlan( final PhysicalOperator pop,
	                                       final PhysicalPlan... subplans ) {
		return createPlan(pop, null, subplans);
	}

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplans become children of the root operator.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the plan is indeed meant to be associated with it (for instance,
	 * when creating a physical plan for a logical plan that is associated
	 * with this object). Also, do not create different physical plans with
	 * the same {@link QueryPlanningInfo} object because these objects may
	 * later be extended with additional properties for each plan; instead,
	 * make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlan( final PhysicalOperator pop,
	                                       final QueryPlanningInfo qpInfo,
	                                       final PhysicalPlan... subplans ) {
		if (      pop instanceof NullaryPhysicalOp nullaryPOP ) return createPlan(nullaryPOP, qpInfo);
		else if ( pop instanceof UnaryPhysicalOp unaryPOP )     return createPlan(unaryPOP, qpInfo, subplans[0]);
		else if ( pop instanceof BinaryPhysicalOp binaryPOP )   return createPlan(binaryPOP, qpInfo, subplans[0], subplans[1]);
		else if ( pop instanceof NaryPhysicalOp naryPOP )       return createPlan(naryPOP, qpInfo, subplans);
		else throw new UnsupportedOperationException("Unsupported type of physical operator: " + pop.getClass().getName() + ".");
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
	 * Creates a plan with a request operator as root operator.
	 */
	public static  PhysicalPlan createPlanWithRequest( final DataRetrievalRequest req,
	                                                   final FederationMember fm ) {
		return createPlanWithRequest( new LogicalOpRequest<>(fm, req) );
	}

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the plan is indeed meant to be associated with it (for instance,
	 * when creating a physical plan for a logical plan that is associated
	 * with this object). Also, do not create different physical plans with
	 * the same {@link QueryPlanningInfo} object because these objects may
	 * later be extended with additional properties for each plan; instead,
	 * make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlan( final NullaryLogicalOp rootOp,
	                                       final QueryPlanningInfo qpInfo ) {
		final NullaryPhysicalOp pop = LogicalToPhysicalOpConverter.convert(rootOp);
		return createPlan(pop, qpInfo);
	}

	/**
	 * Creates a physical plan with the given root operator.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the plan is indeed meant to be associated with it (for instance,
	 * when creating a physical plan for a logical plan that is associated
	 * with this object). Also, do not create different physical plans with
	 * the same {@link QueryPlanningInfo} object because these objects may
	 * later be extended with additional properties for each plan; instead,
	 * make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlan( final NullaryPhysicalOp rootOp,
	                                       final QueryPlanningInfo qpInfo ) {
		if ( qpInfo != null )
			return new PhysicalPlanWithNullaryRootImpl(rootOp, qpInfo) {};
		else
			return new PhysicalPlanWithNullaryRootImpl(rootOp) {};
	}


	// --------- plans with unary root operators -----------

	/**
	 * Creates a plan with a bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoin( final LogicalOpGPAdd lop,
	                                                   final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpBindJoin(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with an index nested loops join as root operator.
	 */
	public static PhysicalPlan createPlanWithIndexNLJ( final LogicalOpGPAdd lop,
	                                                   final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpIndexNestedLoopsJoin(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a FILTER-based bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinFILTER( final LogicalOpGPAdd lop,
	                                                         final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpBindJoinWithFILTER(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a UNION-based bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinUNION( final LogicalOpGPAdd lop,
	                                                        final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpBindJoinWithUNION(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a VALUES-based bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinVALUES( final LogicalOpGPAdd lop,
	                                                         final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpBindJoinWithVALUES(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a VALUES-based bind join that can switch to FILTER-based bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinVALUESorFILTER( final LogicalOpGPAdd lop,
	                                                                 final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpBindJoinWithVALUESorFILTER(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a bound join-based bind join.
	 */
	public static PhysicalPlan createPlanWithBindJoinBoundJoin( final LogicalOpGPAdd lop,
	                                                            final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = new PhysicalOpBindJoinWithBoundJoin(lop);
		return createPlan(pop, subplan);
	}

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplan becomes the single child of the root operator.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the plan is indeed meant to be associated with it (for instance,
	 * when creating a physical plan for a logical plan that is associated
	 * with this object). Also, do not create different physical plans with
	 * the same {@link QueryPlanningInfo} object because these objects may
	 * later be extended with additional properties for each plan; instead,
	 * make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlan( final UnaryLogicalOp rootOp,
	                                       final QueryPlanningInfo qpInfo,
	                                       final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop = LogicalToPhysicalOpConverter.convert(rootOp);
		return createPlan(pop, qpInfo, subplan);
	}

	/**
	 * Creates a physical plan with the given root operator. The
	 * given subplan becomes the single child of the root operator.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the plan is indeed meant to be associated with it (for instance,
	 * when creating a physical plan for a logical plan that is associated
	 * with this object). Also, do not create different physical plans with
	 * the same {@link QueryPlanningInfo} object because these objects may
	 * later be extended with additional properties for each plan; instead,
	 * make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlan( final UnaryPhysicalOp rootOp,
	                                       final QueryPlanningInfo qpInfo,
	                                       final PhysicalPlan subplan ) {
		if ( qpInfo != null )
			return new PhysicalPlanWithUnaryRootImpl(rootOp, qpInfo, subplan) {};
		else
			return new PhysicalPlanWithUnaryRootImpl(rootOp, subplan) {};
	}

	// --------- plans with binary root operators -----------

	/**
	 * Creates a plan with a binary union as root operator.
	 */
	public static PhysicalPlan createPlanWithUnion( final PhysicalPlan subplan1,
	                                                final PhysicalPlan subplan2 ) {
		return createPlanWithUnion( LogicalOpUnion.getInstance(), subplan1, subplan2 );
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
		return createPlanWithHashJoin( LogicalOpJoin.getInstance(), subplan1, subplan2 );
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
		return createPlanWithSymmetricHashJoin( LogicalOpJoin.getInstance(), subplan1, subplan2 );
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
		return createPlanWithNaiveNLJ( LogicalOpJoin.getInstance(), subplan1, subplan2 );
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
	 * Creates a plan with a binary join as root operator, using
	 * the default physical operator for such a join (as per
	 * {@link LogicalToPhysicalOpConverter}).
	 */
	public static PhysicalPlan createPlanWithJoin( final PhysicalPlan subplan1,
	                                               final PhysicalPlan subplan2 ) {
		return createPlanWithJoin(subplan1, subplan2, null);
	}

	/**
	 * Creates a plan with a binary join as root operator, using
	 * the default physical operator for such a join (as per
	 * {@link LogicalToPhysicalOpConverter}).
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the resulting plan is indeed meant to be associated with it (for
	 * instance, when creating a physical plan for a logical plan that is
	 * associated with this object). Also, do not create different physical
	 * plans with the same {@link QueryPlanningInfo} object because these
	 * objects may later be extended with additional properties for each plan;
	 * instead, make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlanWithJoin( final PhysicalPlan subplan1,
	                                               final PhysicalPlan subplan2,
	                                               final QueryPlanningInfo qpInfo ) {
		return createPlan( LogicalOpJoin.getInstance(), qpInfo, subplan1, subplan2 );
	}

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplans become children of the root operator.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the plan is indeed meant to be associated with it (for instance,
	 * when creating a physical plan for a logical plan that is associated
	 * with this object). Also, do not create different physical plans with
	 * the same {@link QueryPlanningInfo} object because these objects may
	 * later be extended with additional properties for each plan; instead,
	 * make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlan( final BinaryLogicalOp rootOp,
	                                       final QueryPlanningInfo qpInfo,
	                                       final PhysicalPlan subplan1,
	                                       final PhysicalPlan subplan2 ) {
		final BinaryPhysicalOp pop = LogicalToPhysicalOpConverter.convert(rootOp);
		return createPlan(pop, qpInfo, subplan1, subplan2);
	}

	/**
	 * Creates a physical plan with the given root operator. The
	 * given subplans become children of the root operator.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the plan is indeed meant to be associated with it (for instance,
	 * when creating a physical plan for a logical plan that is associated
	 * with this object). Also, do not create different physical plans with
	 * the same {@link QueryPlanningInfo} object because these objects may
	 * later be extended with additional properties for each plan; instead,
	 * make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlan( final BinaryPhysicalOp rootOp,
	                                       final QueryPlanningInfo qpInfo,
	                                       final PhysicalPlan subplan1,
	                                       final PhysicalPlan subplan2 ) {
		if ( qpInfo != null )
			return new PhysicalPlanWithBinaryRootImpl(rootOp, qpInfo, subplan1, subplan2) {};
		else
			return new PhysicalPlanWithBinaryRootImpl(rootOp, subplan1, subplan2) {};
	}


	// --------- plans with n-ary root operators -----------

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplans become children of the root operator.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the plan is indeed meant to be associated with it (for instance,
	 * when creating a physical plan for a logical plan that is associated
	 * with this object). Also, do not create different physical plans with
	 * the same {@link QueryPlanningInfo} object because these objects may
	 * later be extended with additional properties for each plan; instead,
	 * make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlan( final NaryLogicalOp rootOp,
	                                       final QueryPlanningInfo qpInfo,
	                                       final PhysicalPlan... subplans ) {
		final NaryPhysicalOp pop = LogicalToPhysicalOpConverter.convert(rootOp);
		return createPlan(pop, qpInfo, subplans);
	}

	/**
	 * Creates a physical plan in which the root operator is the
	 * default physical operator for the given logical operator,
	 * as per {@link LogicalToPhysicalOpConverter}. The given
	 * subplans become children of the root operator.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the plan is indeed meant to be associated with it (for instance,
	 * when creating a physical plan for a logical plan that is associated
	 * with this object). Also, do not create different physical plans with
	 * the same {@link QueryPlanningInfo} object because these objects may
	 * later be extended with additional properties for each plan; instead,
	 * make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlan( final NaryLogicalOp rootOp,
	                                       final QueryPlanningInfo qpInfo,
	                                       final List<PhysicalPlan> subplans ) {
		final NaryPhysicalOp pop = LogicalToPhysicalOpConverter.convert(rootOp);
		return createPlan(pop, qpInfo, subplans);
	}

	/**
	 * Creates a physical plan with the given root operator. The
	 * given subplans become children of the root operator.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the plan is indeed meant to be associated with it (for instance,
	 * when creating a physical plan for a logical plan that is associated
	 * with this object). Also, do not create different physical plans with
	 * the same {@link QueryPlanningInfo} object because these objects may
	 * later be extended with additional properties for each plan; instead,
	 * make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlan( final NaryPhysicalOp rootOp,
	                                       final QueryPlanningInfo qpInfo,
	                                       final PhysicalPlan... subplans ) {
		if ( qpInfo != null )
			return new PhysicalPlanWithNaryRootImpl(rootOp, qpInfo, subplans) {};
		else
			return new PhysicalPlanWithNaryRootImpl(rootOp, subplans) {};
	}

	/**
	 * Creates a physical plan with the given root operator. The
	 * given subplans become children of the root operator.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the plan is indeed meant to be associated with it (for instance,
	 * when creating a physical plan for a logical plan that is associated
	 * with this object). Also, do not create different physical plans with
	 * the same {@link QueryPlanningInfo} object because these objects may
	 * later be extended with additional properties for each plan; instead,
	 * make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlan( final NaryPhysicalOp rootOp,
	                                       final QueryPlanningInfo qpInfo,
	                                       final List<PhysicalPlan> subplans ) {
		if ( qpInfo != null )
			return new PhysicalPlanWithNaryRootImpl(rootOp, qpInfo, subplans) {};
		else
			return new PhysicalPlanWithNaryRootImpl(rootOp, subplans) {};
	}


	// --------- other special cases -----------

	public static PhysicalPlan extractRequestAsPlan( final LogicalOpGPAdd gpAdd ) {
		final FederationMember fm = gpAdd.getFederationMember();

		if ( fm.getInterface().supportsSPARQLPatternRequests() ) {
			final SPARQLRequest req = new SPARQLRequestImpl( gpAdd.getPattern() );
			return createPlanWithRequest(req, fm);
		}

		if ( fm.getInterface().supportsTriplePatternRequests() ) {
			final TriplePattern tp = gpAdd.getTP();

			if ( tp == null )
				throw new IllegalArgumentException( "The graph pattern should be a triple pattern, but it is a " + gpAdd.getPattern().getClass().getName() );

			if ( fm instanceof TPFServer || fm instanceof BRTPFServer ) {
				final TPFRequest req = new TPFRequestImpl(tp);
				return createPlanWithRequest(req, fm);
			}
		}

		throw new IllegalArgumentException("Unsupported type of federation member (type: " + fm.getClass().getName() + ").");
	}

	public static PhysicalPlan extractRequestAsPlan( final PhysicalOpBindJoin pop ) {
		return extractRequestAsPlan( pop.getLogicalOperator() );
	}

	public static PhysicalPlan extractRequestAsPlan( final PhysicalOpBindJoinWithFILTER pop ) {
		return extractRequestAsPlan( pop.getLogicalOperator() );
	}

	public static PhysicalPlan extractRequestAsPlan( final PhysicalOpBindJoinWithUNION pop ) {
		return extractRequestAsPlan( pop.getLogicalOperator() );
	}

	public static PhysicalPlan extractRequestAsPlan( final PhysicalOpBindJoinWithVALUES pop ) {
		return extractRequestAsPlan( pop.getLogicalOperator() );
	}

	public static PhysicalPlan extractRequestAsPlan( final PhysicalOpIndexNestedLoopsJoin pop ) {
		return extractRequestAsPlan( pop.getLogicalOperator() );
	}

	public static PhysicalPlan extractRequestAsPlan( final UnaryLogicalOp lop ) {
		if ( lop instanceof LogicalOpGPAdd ) {
			return extractRequestAsPlan( (LogicalOpGPAdd) lop );
		}
		else {
			throw new IllegalArgumentException("Unsupported type of logical operator (type: " + lop.getClass().getName() + ").");
		}
	}

	public static List<PhysicalPlan> enumeratePlansWithUnaryOpFromReq( final PhysicalOpRequest<?, ?> req,
	                                                                   final PhysicalPlan subplan ) {
		final List<PhysicalPlan> plans = new ArrayList<>();
		final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) req).getLogicalOperator();
		final UnaryLogicalOp newRoot = LogicalOpUtils.createLogicalAddOpFromLogicalReqOp( (LogicalOpRequest<?,?>) lop );

		if ( newRoot instanceof LogicalOpGPAdd gpAdd ) {
			plans.add( createPlanWithIndexNLJ( gpAdd, subplan) );

			final FederationMember fm = gpAdd.getFederationMember();

			if ( fm.getInterface().supportsSPARQLPatternRequests() ) {
				plans.add( createPlanWithBindJoinFILTER(gpAdd, subplan) );
				plans.add( createPlanWithBindJoinUNION(gpAdd, subplan) );
				//plans.add( createPlanWithBindJoinVALUES(gpAdd, subplan) );
				plans.add( createPlanWithBindJoinVALUESorFILTER(gpAdd, subplan) );
				plans.add( createPlanWithBindJoinBoundJoin(gpAdd, subplan) );
			}

			if ( fm instanceof BRTPFServer ) {
				plans.add( createPlanWithBindJoin(gpAdd, subplan) );
			}
		}
		else {
			throw new UnsupportedOperationException( "unsupported operator: " + newRoot.getClass().getName() );
		}

		return plans;
	}

	/**
	 * This function takes two physical plans as input, with the assumptions
	 * that the second of these plans i) has a union as its root operator and
	 * ii) every sub plan under this union is either a request or a filter
	 * with a request.
	 *
	 * Given such input plans, the function turns the requests under the union
	 * into gpAdd operators with the first given plan as a common subplan.
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the resulting plan is indeed meant to be associated with it (for
	 * instance, when creating a physical plan for a logical plan that is
	 * associated with this object). Also, do not create different physical
	 * plans with the same {@link QueryPlanningInfo} object because these
	 * objects may later be extended with additional properties for each plan;
	 * instead, make copies of such an object if needed.
	 */
	public static PhysicalPlan createPlanWithUnaryOpForUnionPlan( final PhysicalPlan inputPlan,
	                                                              final PhysicalPlan unionPlan,
	                                                              final QueryPlanningInfo qpInfo ) {
		final int numberOfSubPlansUnderUnion = unionPlan.numberOfSubPlans();
		final List<PhysicalPlan> newUnionSubPlans = new ArrayList<>(numberOfSubPlansUnderUnion);

		for ( int i = 0; i < numberOfSubPlansUnderUnion; i++ ) {
			final PhysicalPlan oldSubPlan = unionPlan.getSubPlan(i);
			final QueryPlanningInfo qpInfoForNewSubPlan = null;
			final PhysicalPlan newSubPlan = createPlanWithDefaultUnaryOpIfPossible(inputPlan, oldSubPlan, qpInfoForNewSubPlan);
			newUnionSubPlans.add(newSubPlan);
		}

		return createPlan( LogicalOpMultiwayUnion.getInstance(), qpInfo, newUnionSubPlans );
	}

	/**
	 * If the second of the two given plans is either a request, a filter
	 * with request, or a union over requests, then this function turns the
	 * request(s) into gpAdd operators with the first given plan as subplan.
	 *
	 * Otherwise, the function returns a plan with a binary join over the two
	 * given plans (using the default physical operator).
	 * <p>
	 * The qpInfo argument may be <code>null</code>. Provide an actual
	 * {@link QueryPlanningInfo} object only if this object already exists
	 * and the resulting plan is indeed meant to be associated with it (for
	 * instance, when creating a physical plan for a logical plan that is
	 * associated with this object). Also, do not create different physical
	 * plans with the same {@link QueryPlanningInfo} object because these
	 * objects may later be extended with additional properties for each plan;
	 * instead, make copies of such an object if needed.
	 **/
	public static PhysicalPlan createPlanWithDefaultUnaryOpIfPossible( final PhysicalPlan inputPlan,
	                                                                   final PhysicalPlan nextPlan,
	                                                                   final QueryPlanningInfo qpInfo ) {
		final PhysicalOperator rootOp = nextPlan.getRootOperator();

		if ( rootOp instanceof PhysicalOpRequest reqOp ) {
			final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp(reqOp);
			return PhysicalPlanFactory.createPlan(addOp, qpInfo, inputPlan);
		}

		final PhysicalOperator subPlanRootOp = nextPlan.getSubPlan(0).getRootOperator();

		if (    rootOp instanceof PhysicalOpFilter filterOp
		     && subPlanRootOp instanceof PhysicalOpRequest reqOp ) {
			final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp(reqOp);
			final PhysicalPlan addOpPlan = PhysicalPlanFactory.createPlan(addOp, inputPlan);
			return PhysicalPlanFactory.createPlan(filterOp, qpInfo, addOpPlan);
		}

		if (    rootOp instanceof PhysicalOpLocalToGlobal l2gPOP
		     && subPlanRootOp instanceof PhysicalOpRequest reqOp ) {
			final LogicalOpLocalToGlobal l2gLOP = (LogicalOpLocalToGlobal) l2gPOP.getLogicalOperator();
			final VocabularyMapping vm = l2gLOP.getVocabularyMapping();

			final LogicalOpGlobalToLocal g2l = new LogicalOpGlobalToLocal(vm);
			final PhysicalPlan newInputPlan = PhysicalPlanFactory.createPlan(g2l, inputPlan);

			final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp(reqOp);
			final PhysicalPlan addOpPlan = PhysicalPlanFactory.createPlan(addOp, newInputPlan);

			return PhysicalPlanFactory.createPlan(l2gPOP, qpInfo, addOpPlan);
		}

		if (    (rootOp instanceof PhysicalOpBinaryUnion || rootOp instanceof PhysicalOpMultiwayUnion)
		     && PhysicalPlanFactory.checkUnaryOpApplicableToUnionPlan(nextPlan) ) {
			return PhysicalPlanFactory.createPlanWithUnaryOpForUnionPlan(inputPlan, nextPlan, qpInfo);
		}

		return createPlanWithJoin(inputPlan, nextPlan, qpInfo);
	}

	/**
	 * Returns <code>true</code> if the given plan has a union operator as
	 * its root operator and all subplans under this union are of one of the
	 * following forms:
	 * i) a request,
	 * ii) a filter over a request,
	 * iii) an l2g operator over either a request or a filter with a request.
	 */
	public static boolean checkUnaryOpApplicableToUnionPlan( final PhysicalPlan unionPlan ){
		final PhysicalOperator rootOp = unionPlan.getRootOperator();
		if (    ! (rootOp instanceof PhysicalOpBinaryUnion)
		     && ! (rootOp instanceof PhysicalOpMultiwayUnion) ) {
			return false;
		}

		for ( int i = 0; i < unionPlan.numberOfSubPlans(); i++ ) {
			final PhysicalPlan subPlan = unionPlan.getSubPlan(i);
			final PhysicalOperator subRootOp = subPlan.getRootOperator();

			if (    ! (subRootOp instanceof PhysicalOpRequest)
			     && ! (subRootOp instanceof PhysicalOpFilter)
			     && ! (subRootOp instanceof PhysicalOpLocalToGlobal) ) {
				return false;
			}

			if ( subRootOp instanceof PhysicalOpLocalToGlobal ) {
				final PhysicalPlan subSubPlan = subPlan.getSubPlan(0);
				final PhysicalOperator subSubRootOp = subSubPlan.getRootOperator();

				if (    ! (subSubRootOp instanceof PhysicalOpRequest)
				     && ! (subSubRootOp instanceof PhysicalOpFilter) ) {
					return false;
				}

				if ( subSubRootOp instanceof PhysicalOpFilter ) {
					final PhysicalOperator subSubSubRootOp = subSubPlan.getSubPlan(0).getRootOperator();
					if ( ! (subSubSubRootOp instanceof PhysicalOpRequest) ) {
						return false;
					}
				}
			}

			if ( subRootOp instanceof PhysicalOpFilter ) {
				final PhysicalOperator subSubRootOp = subPlan.getSubPlan(0).getRootOperator();

				if ( ! (subSubRootOp instanceof PhysicalOpRequest) ) {
					return false;
				}
			}
		}

		return true;
	}

}
