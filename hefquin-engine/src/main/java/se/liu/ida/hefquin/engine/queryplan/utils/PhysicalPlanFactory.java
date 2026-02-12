package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
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
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.TPFServer;

public class PhysicalPlanFactory
{
	/**
	 * Creates a physical plan in which the root operator is the physical
	 * operator that the given {@link LogicalToPhysicalOpConverter} returns
	 * for the given logical operator. The given subplans become children
	 * of the root operator.
	 */
	public static PhysicalPlan createPlan( final LogicalOperator lop,
	                                       final LogicalToPhysicalOpConverter lop2pop,
	                                       final PhysicalPlan... subplans ) {
		return createPlan( lop, null, lop2pop, subplans );
	}

	/**
	 * Creates a physical plan in which the root operator is the physical
	 * operator that the given {@link LogicalToPhysicalOpConverter} returns
	 * for the given logical operator. The given subplans become children
	 * of the root operator.
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
	                                       final LogicalToPhysicalOpConverter lop2pop,
	                                       final PhysicalPlan... subplans ) {
		if (      lop instanceof NullaryLogicalOp nullaryLOP ) return createPlan(nullaryLOP, qpInfo, lop2pop);
		else if ( lop instanceof UnaryLogicalOp unaryLOP )     return createPlan(unaryLOP, qpInfo, lop2pop, subplans[0]);
		else if ( lop instanceof BinaryLogicalOp binaryLOP )   return createPlan(binaryLOP, qpInfo, lop2pop, subplans[0], subplans[1]);
		else if ( lop instanceof NaryLogicalOp naryLOP )       return createPlan(naryLOP, qpInfo, lop2pop, subplans);
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
	public static  PhysicalPlan createPlanWithRequest( final DataRetrievalRequest req,
	                                                   final FederationMember fm ) {
		final LogicalOpRequest<?,?> lop = new LogicalOpRequest<>(fm, req);
		final NullaryPhysicalOp pop = PhysicalOpRequest.getFactory().create(lop);
		return createPlan(pop);
	}

	/**
	 * Creates a physical plan in which the root operator is the physical
	 * operator that the given {@link LogicalToPhysicalOpConverter} returns
	 * for the given logical operator.
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
	                                       final QueryPlanningInfo qpInfo,
	                                       final LogicalToPhysicalOpConverter lop2pop ) {
		final NullaryPhysicalOp pop = lop2pop.convert(rootOp);
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
	 * Creates a physical plan in which the root operator is the physical
	 * operator that the given {@link LogicalToPhysicalOpConverter} returns
	 * for the given logical operator. The given subplan becomes the child
	 * of the root operator.
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
	                                       final LogicalToPhysicalOpConverter lop2pop,
	                                       final PhysicalPlan subplan ) {
		final ExpectedVariables inputVars = subplan.getExpectedVariables();
		final UnaryPhysicalOp pop = lop2pop.convert(rootOp, inputVars);
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
	 * Creates a plan with a binary join as root operator, using the
	 * default physical operator for such a join (as per the given
	 * {@link LogicalToPhysicalOpConverter}).
	 */
	public static PhysicalPlan createPlanWithJoin( final PhysicalPlan subplan1,
	                                               final PhysicalPlan subplan2,
	                                               final LogicalToPhysicalOpConverter lop2pop ) {
		return createPlanWithJoin(subplan1, subplan2, null, lop2pop);
	}

	/**
	 * Creates a plan with a binary join as root operator, using the
	 * default physical operator for such a join (as per the given
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
	                                               final QueryPlanningInfo qpInfo,
	                                               final LogicalToPhysicalOpConverter lop2pop ) {
		return createPlan( LogicalOpJoin.getInstance(), qpInfo, lop2pop, subplan1, subplan2 );
	}

	/**
	 * Creates a physical plan in which the root operator is the physical
	 * operator that the given {@link LogicalToPhysicalOpConverter} returns
	 * for the given logical operator. The given subplans become children
	 * of the root operator.
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
	                                       final LogicalToPhysicalOpConverter lop2pop,
	                                       final PhysicalPlan subplan1,
	                                       final PhysicalPlan subplan2 ) {
		final ExpectedVariables inputVars1 = subplan1.getExpectedVariables();
		final ExpectedVariables inputVars2 = subplan2.getExpectedVariables();
		final BinaryPhysicalOp pop = lop2pop.convert(rootOp, inputVars1, inputVars2);
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
	 * Creates a physical plan in which the root operator is the physical
	 * operator that the given {@link LogicalToPhysicalOpConverter} returns
	 * for the given logical operator. The given subplans become children
	 * of the root operator.
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
	                                       final LogicalToPhysicalOpConverter lop2pop,
	                                       final PhysicalPlan... subplans ) {
		// Collect input vars
		final ExpectedVariables[] inputVars = Arrays.stream(subplans)
				.map( PhysicalPlan::getExpectedVariables )
				.toArray( ExpectedVariables[]::new );

		final NaryPhysicalOp pop = lop2pop.convert(rootOp, inputVars);
		return createPlan(pop, qpInfo, subplans);
	}

	/**
	 * Creates a physical plan in which the root operator is the physical
	 * operator that the given {@link LogicalToPhysicalOpConverter} returns
	 * for the given logical operator. The given subplans become children
	 * of the root operator.
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
	                                       final LogicalToPhysicalOpConverter lop2pop,
	                                       final List<PhysicalPlan> subplans ) {
		// Collect input vars
		final ExpectedVariables[] inputVars = subplans.stream()
				.map( PhysicalPlan::getExpectedVariables )
				.toArray( ExpectedVariables[]::new );

		final NaryPhysicalOp pop = lop2pop.convert(rootOp, inputVars);
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
		final SPARQLGraphPattern gp = gpAdd.getPattern();

		if ( fm instanceof TPFServer || fm instanceof BRTPFServer ) {
			final TriplePattern tp = gpAdd.getTP();
			final TPFRequest req = new TPFRequestImpl(tp);
			return createPlanWithRequest(req, fm);
		}
		else if ( fm.isSupportedPattern(gp) ) {
			final SPARQLRequest req = new SPARQLRequestImpl(gp);
			return createPlanWithRequest(req, fm);
		}
		else {
			throw new IllegalArgumentException("Unsupported type of federation member (type: " + fm.getClass().getName() + ").");
		}
	}

	public static PhysicalPlan extractRequestAsPlan( final PhysicalOpBindJoinBRTPF pop ) {
		return extractRequestAsPlan( pop.getLogicalOperator() );
	}

	public static PhysicalPlan extractRequestAsPlan( final PhysicalOpBindJoinSPARQL pop ) {
		return extractRequestAsPlan( pop.getLogicalOperator() );
	}

	public static PhysicalPlan extractRequestAsPlan( final PhysicalOpIndexNestedLoopsJoin pop ) {
		return extractRequestAsPlan( pop.getLogicalOperator() );
	}

	public static PhysicalPlan extractRequestAsPlan( final UnaryLogicalOp lop ) {
		if ( lop instanceof LogicalOpGPAdd gpAdd ) {
			return extractRequestAsPlan(gpAdd);
		}
		else {
			throw new IllegalArgumentException("Unsupported type of logical operator (type: " + lop.getClass().getName() + ").");
		}
	}

	public static List<PhysicalPlan> enumeratePlansWithUnaryOpFromReq(
			final PhysicalOpRequest<?, ?> req,
			final PhysicalPlan subplan,
			final LogicalToPhysicalOpConverter lop2pop ) {
		final LogicalOpRequest<?,?> lop = (LogicalOpRequest<?,?>) req.getLogicalOperator();
		final UnaryLogicalOp newRoot = LogicalOpUtils.createLogicalAddOpFromLogicalReqOp(lop);

		assert newRoot instanceof LogicalOpGPAdd;

		final Set<UnaryPhysicalOp> allPOPs = lop2pop.getAllPossible( newRoot,
		                                                             subplan.getExpectedVariables() );

		final List<PhysicalPlan> plans = new ArrayList<>( allPOPs.size() );
		for ( final UnaryPhysicalOp pop : allPOPs ) {
			plans.add( createPlan(pop, subplan) );
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
	                                                              final QueryPlanningInfo qpInfo,
	                                                              final LogicalToPhysicalOpConverter lop2pop ) {
		final int numberOfSubPlansUnderUnion = unionPlan.numberOfSubPlans();
		final List<PhysicalPlan> newUnionSubPlans = new ArrayList<>(numberOfSubPlansUnderUnion);

		for ( int i = 0; i < numberOfSubPlansUnderUnion; i++ ) {
			final PhysicalPlan oldSubPlan = unionPlan.getSubPlan(i);
			final QueryPlanningInfo qpInfoForNewSubPlan = null;
			final PhysicalPlan newSubPlan = createPlanWithDefaultUnaryOpIfPossible(inputPlan, oldSubPlan, qpInfoForNewSubPlan, lop2pop);
			newUnionSubPlans.add(newSubPlan);
		}

		return createPlan( LogicalOpMultiwayUnion.getInstance(), qpInfo, lop2pop, newUnionSubPlans );
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
	                                                                   final QueryPlanningInfo qpInfo,
	                                                                   final LogicalToPhysicalOpConverter lop2pop ) {
		final PhysicalOperator rootOp = nextPlan.getRootOperator();

		if ( rootOp instanceof PhysicalOpRequest reqOp ) {
			final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp(reqOp);
			return PhysicalPlanFactory.createPlan(addOp, qpInfo, lop2pop, inputPlan);
		}

		final PhysicalOperator subPlanRootOp = nextPlan.getSubPlan(0).getRootOperator();

		if (    rootOp instanceof PhysicalOpFilter filterOp
		     && subPlanRootOp instanceof PhysicalOpRequest reqOp ) {
			final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp(reqOp);
			final PhysicalPlan addOpPlan = PhysicalPlanFactory.createPlan(addOp, lop2pop, inputPlan);
			return PhysicalPlanFactory.createPlan(filterOp, qpInfo, addOpPlan);
		}

		if (    rootOp instanceof PhysicalOpLocalToGlobal l2gPOP
		     && subPlanRootOp instanceof PhysicalOpRequest reqOp ) {
			final LogicalOpLocalToGlobal l2gLOP = (LogicalOpLocalToGlobal) l2gPOP.getLogicalOperator();
			final VocabularyMapping vm = l2gLOP.getVocabularyMapping();

			final LogicalOpGlobalToLocal g2l = new LogicalOpGlobalToLocal(vm);
			final PhysicalPlan newInputPlan = PhysicalPlanFactory.createPlan(g2l, lop2pop, inputPlan);

			final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp(reqOp);
			final PhysicalPlan addOpPlan = PhysicalPlanFactory.createPlan(addOp, lop2pop, newInputPlan);

			return PhysicalPlanFactory.createPlan(l2gPOP, qpInfo, addOpPlan);
		}

		if (    (rootOp instanceof PhysicalOpBinaryUnion || rootOp instanceof PhysicalOpMultiwayUnion)
		     && PhysicalPlanFactory.checkUnaryOpApplicableToUnionPlan(nextPlan) ) {
			return PhysicalPlanFactory.createPlanWithUnaryOpForUnionPlan(inputPlan, nextPlan, qpInfo, lop2pop);
		}

		return createPlanWithJoin(inputPlan, nextPlan, qpInfo, lop2pop);
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
