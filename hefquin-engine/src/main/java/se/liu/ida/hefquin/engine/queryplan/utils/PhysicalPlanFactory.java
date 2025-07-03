package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.query.TriplePattern;
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
	 * Creates a plan with a binary join as root operator.
	 * The root operator is the default physical operator
	 */
	public static PhysicalPlan createPlanWithJoin( final PhysicalPlan subplan1,
													final PhysicalPlan subplan2 ) {
		return createPlan( LogicalOpJoin.getInstance(), subplan1, subplan2 );
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
	 * This function take a inputPlan and unionPlan as input,
	 * where the unionPlan is required to be a plan with union as root operator, and all subPlans under the UNION are all requests or filters with request.
	 *
	 * In such cases, this function turns the requests under UNION into xxAdd operators with the inputPlan as subplans.
	 */
	public static PhysicalPlan createPlanWithUnaryOpForUnionPlan( final PhysicalPlan inputPlan, final PhysicalPlan unionPlan ) {
		final int numberOfSubPlansUnderUnion = unionPlan.numberOfSubPlans();
		final PhysicalPlan[] newUnionSubPlans = new PhysicalPlan[numberOfSubPlansUnderUnion];

		for ( int i = 0; i < numberOfSubPlansUnderUnion; i++ ) {
			final PhysicalPlan oldSubPlan = unionPlan.getSubPlan(i);
			final PhysicalPlan newSubPlan = createPlanWithDefaultUnaryOpIfPossible( inputPlan, oldSubPlan );
			newUnionSubPlans[i] = newSubPlan;
		}

		return createPlan( LogicalOpMultiwayUnion.getInstance(), newUnionSubPlans );
	}

	/**
	 * If the nextPlan is in the form of a request, filter with request, or union with requests,
	 * this function turns the requests into xxAdd operators with the inputPlan as subplans.
	 *
	 * Otherwise, it constructs a plan with a binary join between inputPlan and nextPlan (using the default physical operator)
	 **/
	public static PhysicalPlan createPlanWithDefaultUnaryOpIfPossible( final PhysicalPlan inputPlan, final PhysicalPlan nextPlan ) {
		final PhysicalOperator oldSubPlanRootOp = nextPlan.getRootOperator();
		if ( oldSubPlanRootOp instanceof PhysicalOpRequest ) {
			final PhysicalOpRequest<?,?> reqOp = (PhysicalOpRequest<?,?>) oldSubPlanRootOp;
			final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp(reqOp);
			return PhysicalPlanFactory.createPlan( addOp, inputPlan);
		}
		else if ( oldSubPlanRootOp instanceof PhysicalOpFilter
				&& nextPlan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest ) {
			final PhysicalOpFilter filterOp = (PhysicalOpFilter) oldSubPlanRootOp;
			final PhysicalOpRequest<?,?> reqOp = (PhysicalOpRequest<?,?>) nextPlan.getSubPlan(0).getRootOperator();

			final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp(reqOp);
			final PhysicalPlan addOpPlan = PhysicalPlanFactory.createPlan( addOp, inputPlan);

			return PhysicalPlanFactory.createPlan( filterOp, addOpPlan);
		}
		else if ( oldSubPlanRootOp instanceof PhysicalOpLocalToGlobal
				&& nextPlan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest ){
			final PhysicalOpLocalToGlobal l2gPOP = (PhysicalOpLocalToGlobal) oldSubPlanRootOp;
			final LogicalOpLocalToGlobal l2gLOP = (LogicalOpLocalToGlobal) l2gPOP.getLogicalOperator();
			final VocabularyMapping vm = l2gLOP.getVocabularyMapping();

			final LogicalOpGlobalToLocal g2l = new LogicalOpGlobalToLocal(vm);
			final PhysicalPlan newInputPlan = PhysicalPlanFactory.createPlan( new PhysicalOpGlobalToLocal(g2l), inputPlan );

			final PhysicalOpRequest<?,?> reqOp = (PhysicalOpRequest<?,?>) nextPlan.getSubPlan(0).getRootOperator();

			final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp(reqOp);
			final PhysicalPlan addOpPlan = PhysicalPlanFactory.createPlan( addOp, newInputPlan);

			return PhysicalPlanFactory.createPlan( l2gPOP, addOpPlan );
		}
		else if ( (oldSubPlanRootOp instanceof PhysicalOpBinaryUnion || oldSubPlanRootOp instanceof PhysicalOpMultiwayUnion)
				&& PhysicalPlanFactory.checkUnaryOpApplicableToUnionPlan(nextPlan)){
			
			return PhysicalPlanFactory.createPlanWithUnaryOpForUnionPlan( inputPlan, nextPlan );
		}
		else
			return createPlanWithJoin(inputPlan, nextPlan);
	}

	/**
	 * Check whether all operators under the UNION operator belong to any of the following:
	 * 	 - The operator is a request
	 * 	 - If the operator is a filter, then under that filter there must be a request,
	 * 	 - If the operator is a L2G operator, under the L2G operator, there must be a request or a filter operator with requests.
	 */
	public static boolean checkUnaryOpApplicableToUnionPlan( final PhysicalPlan unionPlan ){
		final PhysicalOperator rootOp = unionPlan.getRootOperator();
		if ( !(rootOp instanceof PhysicalOpBinaryUnion || rootOp instanceof PhysicalOpMultiwayUnion) ){
			return false;
		}

		for ( int i = 0; i < unionPlan.numberOfSubPlans(); i++ ) {
			final PhysicalPlan subPlan = unionPlan.getSubPlan(i);
			final PhysicalOperator subRootOp = subPlan.getRootOperator();
			if ( !(subRootOp instanceof PhysicalOpRequest || subRootOp instanceof PhysicalOpFilter || subRootOp instanceof PhysicalOpLocalToGlobal ) ) {
				return false;
			}

			if ( subRootOp instanceof PhysicalOpLocalToGlobal ){
				final PhysicalPlan subSubPlan = subPlan.getSubPlan(0);
				final PhysicalOperator subSubRootOp = subSubPlan.getRootOperator();
				if ( !( subSubRootOp instanceof PhysicalOpRequest || subSubRootOp instanceof PhysicalOpFilter) ){
					return false;
				}
				if ( subSubRootOp instanceof PhysicalOpFilter ){
					if ( !( subSubPlan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest) ){
						return false;
					}
				}
			}

			if ( subRootOp instanceof PhysicalOpFilter ){
				if ( !( subPlan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest) ){
					return false;
				}
			}
		}
		return true;
	}

}
