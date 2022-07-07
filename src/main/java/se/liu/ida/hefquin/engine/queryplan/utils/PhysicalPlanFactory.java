package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.List;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
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
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules.IdentifyTypeOfRequestUsedForReq;

public class PhysicalPlanFactory
{
	public static boolean handleVocabMappingsExplicitly = true;

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
		final NullaryPhysicalOp pop;
		if ( ! handleVocabMappingsExplicitly && lop.getFederationMember().getVocabularyMapping() != null )
			pop = new PhysicalOpRequestWithTranslation<>(lop);
		else
			pop = new PhysicalOpRequest<>(lop);

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
		final UnaryPhysicalOp pop;
		if ( ! handleVocabMappingsExplicitly && lop.getFederationMember().getVocabularyMapping() != null )
			throw new UnsupportedOperationException("There is no such bind join operator that takes care of vocab.mappings.");
		else
			pop = new PhysicalOpBindJoin(lop);

		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinFILTER( final LogicalOpTPAdd lop,
	                                                         final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop;
		if ( ! handleVocabMappingsExplicitly && lop.getFederationMember().getVocabularyMapping() != null )
			pop = new PhysicalOpBindJoinWithFILTERandTranslation(lop);
		else
			pop = new PhysicalOpBindJoinWithFILTER(lop);

		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinUNION( final LogicalOpTPAdd lop,
	                                                        final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop;
		if ( ! handleVocabMappingsExplicitly && lop.getFederationMember().getVocabularyMapping() != null )
			throw new UnsupportedOperationException("There is no such bind join operator that takes care of vocab.mappings.");
		else
			pop = new PhysicalOpBindJoinWithUNION(lop);

		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinVALUES( final LogicalOpTPAdd lop,
	                                                         final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop;
		if ( ! handleVocabMappingsExplicitly && lop.getFederationMember().getVocabularyMapping() != null )
			throw new UnsupportedOperationException("There is no such bind join operator that takes care of vocab.mappings.");
		else
			pop = new PhysicalOpBindJoinWithVALUES(lop);

		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with an index nested loops join as root operator.
	 */
	public static PhysicalPlan createPlanWithIndexNLJ( final LogicalOpTPAdd lop,
	                                                   final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop;
		if ( ! handleVocabMappingsExplicitly && lop.getFederationMember().getVocabularyMapping() != null )
			throw new UnsupportedOperationException("There is no such join operator that takes care of vocab.mappings.");
		else
			pop = new PhysicalOpIndexNestedLoopsJoin(lop);

		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with an index nested loops join as root operator.
	 */
	public static PhysicalPlan createPlanWithIndexNLJ( final LogicalOpBGPAdd lop,
	                                                   final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop;
		if ( ! handleVocabMappingsExplicitly && lop.getFederationMember().getVocabularyMapping() != null )
			throw new UnsupportedOperationException("There is no such join operator that takes care of vocab.mappings.");
		else
			pop = new PhysicalOpIndexNestedLoopsJoin(lop);

		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinFILTER( final LogicalOpBGPAdd lop,
	                                                         final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop;
		if ( ! handleVocabMappingsExplicitly && lop.getFederationMember().getVocabularyMapping() != null )
			pop = new PhysicalOpBindJoinWithFILTERandTranslation(lop);
		else
			pop = new PhysicalOpBindJoinWithFILTER(lop);

		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinUNION( final LogicalOpBGPAdd lop,
	                                                        final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop;
		if ( ! handleVocabMappingsExplicitly && lop.getFederationMember().getVocabularyMapping() != null )
			throw new UnsupportedOperationException("There is no such bind join operator that takes care of vocab.mappings.");
		else
			pop = new PhysicalOpBindJoinWithUNION(lop);

		return createPlan(pop, subplan);
	}

	/**
	 * Creates a plan with a bind join as root operator.
	 */
	public static PhysicalPlan createPlanWithBindJoinVALUES( final LogicalOpBGPAdd lop,
	                                                         final PhysicalPlan subplan ) {
		final UnaryPhysicalOp pop;
		if ( ! handleVocabMappingsExplicitly && lop.getFederationMember().getVocabularyMapping() != null )
			throw new UnsupportedOperationException("There is no such bind join operator that takes care of vocab.mappings.");
		else
			pop = new PhysicalOpBindJoinWithVALUES(lop);

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

	/**
	 * Creates a physical plan with a bgpAdd as root operator.
	 * The root operator uses the same physical algorithm as the given physical operator, which is usually a specific physical operator for bgpAdd.
	 * The given subplan becomes the single child of the root operator.
	 */
	public static PhysicalPlan createPlanBasedOnTypeOfGivenPhysicalOp( final LogicalOpBGPAdd lop, final Class<? extends PhysicalOperator> opClass, final PhysicalPlan subplan ) {
		if ( PhysicalOpIndexNestedLoopsJoin.class.isAssignableFrom(opClass) ) {
			return createPlanWithIndexNLJ( lop, subplan );
		}
		else if ( PhysicalOpBindJoinWithFILTER.class.isAssignableFrom(opClass) ) {
			return createPlanWithBindJoinFILTER( lop, subplan );
		}
		else if ( PhysicalOpBindJoinWithUNION.class.isAssignableFrom(opClass) ) {
			return createPlanWithBindJoinUNION( lop, subplan );
		}
		else if ( PhysicalOpBindJoinWithVALUES.class.isAssignableFrom(opClass) ) {
			return createPlanWithBindJoinVALUES( lop, subplan );
		}
		else {
			throw new IllegalArgumentException("Unsupported type of physical operator: " + opClass.getName() + ".");
		}
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

	public static PhysicalPlan extractRequestAsPlan( final LogicalOpTPAdd lop ) {
		final FederationMember fm = lop.getFederationMember();
		final TriplePattern tp = lop.getTP();

		final DataRetrievalRequest req;
		if      ( fm instanceof SPARQLEndpoint ) req = new TriplePatternRequestImpl(tp);
		else if ( fm instanceof TPFServer )      req = new TPFRequestImpl(tp);
		else if ( fm instanceof BRTPFServer )    req = new TPFRequestImpl(tp);
		else {
			throw new IllegalArgumentException("Unsupported type of federation member (type: " + fm.getClass().getName() + ").");
		}

		return createPlanWithRequest( new LogicalOpRequest<>(fm,req) );
	}

	public static PhysicalPlan extractRequestAsPlan( final LogicalOpBGPAdd lop ) {
		final FederationMember fm = lop.getFederationMember();

		final DataRetrievalRequest req;
		if ( fm.getInterface().supportsBGPRequests() ) {
			req = new BGPRequestImpl( lop.getBGP() );
		}
		else {
			throw new IllegalArgumentException("Unsupported type of federation member (type: " + fm.getClass().getName() + ").");
		}

		return createPlanWithRequest( new LogicalOpRequest<>(fm,req) );
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

	protected static PhysicalPlan extractRequestAsPlan( final LogicalOperator lop ) {
		if ( lop instanceof LogicalOpTPAdd ) {
			return extractRequestAsPlan( (LogicalOpTPAdd) lop );
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			return extractRequestAsPlan( (LogicalOpBGPAdd) lop );
		}
		else {
			throw new IllegalArgumentException("Unsupported type of logical operator (type: " + lop.getClass().getName() + ").");
		}
	}

	public static List<PhysicalPlan> enumeratePlansWithUnaryOpFromReq( final PhysicalOpRequest<?, ?> req,
	                                                                   final PhysicalPlan subplan,
	                                                                   final List<PhysicalPlan> plans ) {
		if (IdentifyTypeOfRequestUsedForReq.isBGPRequestOverSPARQLEndpoint(req)) {
			final LogicalOpBGPAdd newRoot = (LogicalOpBGPAdd) LogicalOpUtils.createUnaryLopFromReq(req);

			plans.add( createPlanWithIndexNLJ(newRoot, subplan) );
			plans.add( createPlanWithBindJoinFILTER(newRoot, subplan) );
			plans.add( createPlanWithBindJoinUNION(newRoot, subplan) );
			plans.add( createPlanWithBindJoinVALUES(newRoot, subplan) );

		}
		else if (IdentifyTypeOfRequestUsedForReq.isTriplePatternRequest(req)) {
			final LogicalOpTPAdd newRoot = (LogicalOpTPAdd) LogicalOpUtils.createUnaryLopFromReq(req);
			plans.add( createPlanWithIndexNLJ(newRoot, subplan) );

			final FederationMember fm = ((LogicalOpRequest<?, ?>) req.getLogicalOperator()).getFederationMember();
			if (fm instanceof SPARQLEndpoint) {
				plans.add( createPlanWithBindJoinFILTER(newRoot, subplan) );
				plans.add( createPlanWithBindJoinUNION(newRoot, subplan) );
				plans.add( createPlanWithBindJoinVALUES(newRoot, subplan) );
			}
		}
		return plans;
	}

	public static List<PhysicalPlan> enumeratePlansWithUnaryOpFromReq( final PhysicalOpRequestWithTranslation<?, ?> req,
	                                                                   final PhysicalPlan subplan,
	                                                                   final List<PhysicalPlan> plans ) {
		if (IdentifyTypeOfRequestUsedForReq.isBGPRequestOverSPARQLEndpoint(req)) {
			final LogicalOpBGPAdd newRoot = (LogicalOpBGPAdd) LogicalOpUtils.createUnaryLopFromReq(req);

			plans.add( createPlanWithIndexNLJ(newRoot, subplan) );
			plans.add( createPlanWithBindJoinFILTER(newRoot, subplan) );
			plans.add( createPlanWithBindJoinUNION(newRoot, subplan) );
			plans.add( createPlanWithBindJoinVALUES(newRoot, subplan) );

		}
		else if (IdentifyTypeOfRequestUsedForReq.isTriplePatternRequest(req)) {
			final LogicalOpTPAdd newRoot = (LogicalOpTPAdd) LogicalOpUtils.createUnaryLopFromReq(req);
			plans.add( createPlanWithIndexNLJ(newRoot, subplan) );

			final FederationMember fm = ((LogicalOpRequest<?, ?>) req.getLogicalOperator()).getFederationMember();
			if (fm instanceof SPARQLEndpoint) {
				plans.add( createPlanWithBindJoinFILTER(newRoot, subplan) );
				plans.add( createPlanWithBindJoinUNION(newRoot, subplan) );
				plans.add( createPlanWithBindJoinVALUES(newRoot, subplan) );
			}
		}
		return plans;
	}

}
