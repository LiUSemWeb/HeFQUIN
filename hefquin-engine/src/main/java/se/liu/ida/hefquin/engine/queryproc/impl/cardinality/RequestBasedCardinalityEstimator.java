package se.liu.ida.hefquin.engine.queryproc.impl.cardinality;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Quality;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNullaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFixedSolMap;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithNullaryRoot;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpFixedSolMap;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpMultiRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryproc.CardinalityEstimator;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.members.RDFBasedFederationMember;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;

import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.CARDINALITY;

public class RequestBasedCardinalityEstimator implements CardinalityEstimator
{
	@Override
	public void addCardinalities( final QueryProcContext ctx,
	                              final LogicalPlan ... plans ) {
		// As a first step, make sure that all nullary subplans (i.e.,
		// with request operators) within the given plans are annotated
		// with a cardinality estimate.
		addCardinalitiesForRequests(ctx, plans);

		// Now, use the worker to determine the cardinality estimates
		// for the given plans recursively. 
		new CardinalityEstimationWorkerImpl().addCardinalities(plans);
	}

	@Override
	public void addCardinalities( final QueryProcContext ctx,
	                              final PhysicalPlan ... plans ) {
		// As a first step, make sure that all nullary subplans (i.e.,
		// with request operators) within the given plans are annotated
		// with a cardinality estimate.
		addCardinalitiesForRequests(ctx, plans);

		// Now, use the worker to determine the cardinality estimates
		// for the given plans recursively. 
		new CardinalityEstimationWorkerImpl().addCardinalities(plans);
	}


	/**
	 * Makes sure that every nullary subplan within each of the
	 * given plans is annotated with cardinality estimates (which
	 * are determined by issuing cardinality requests).
	 */
	public void addCardinalitiesForRequests( final QueryProcContext ctx,
	                                         final LogicalPlan ... plans ) {
		// Recursively extract all relevant nullary subplans from the
		// given plan, where a nullary subplan is considered relevant
		// if it does not yet have a cardinality estimate.
		final Set<LogicalPlan> subPlans = extractNullarySubPlans(plans);

		// Without any relevant nullary subplans there is nothing to do here.
		if ( subPlans.isEmpty() ) return;

		// As input for the method that adds cardinality estimates to
		// the extracted subplans, obtain the request operator and the
		// QueryPlanningInfo object of each of these subplans (and,
		// also, handle the special case of fixed-input operators).
		List<LogicalOpRequest<?,?>> reqOps  = null;
		List<LogicalOpMultiRequest> mreqOps = null;
		List<QueryPlanningInfo> infoObjs1 = null;
		List<QueryPlanningInfo> infoObjs2 = null;
		for ( final LogicalPlan subPlan : subPlans )
		{
			final LogicalOperator rootOp = subPlan.getRootOperator();
			if ( rootOp instanceof LogicalOpMultiRequest mreqOp ) {
				if ( mreqOps == null ) {
					mreqOps   = new ArrayList<>( subPlans.size() );
					infoObjs2 = new ArrayList<>( subPlans.size() );
				}

				mreqOps.add( mreqOp );
				infoObjs2.add( subPlan.getQueryPlanningInfo() );
			}
			else if ( rootOp instanceof LogicalOpRequest reqOp
			       && reqOp.getFederationMember() instanceof RDFBasedFederationMember ) {
				if ( reqOps == null ) {
					reqOps    = new ArrayList<>( subPlans.size() );
					infoObjs1 = new ArrayList<>( subPlans.size() );
				}

				reqOps.add( reqOp );
				infoObjs1.add( subPlan.getQueryPlanningInfo() );
			}
			else if ( rootOp instanceof LogicalOpRequest reqOp ) {
				addCardinalityForRequestViaWrapper( subPlan.getQueryPlanningInfo(), reqOp );
			}
			else if ( rootOp instanceof LogicalOpFixedSolMap ) {
				addCardinalityForFixedInputOps( subPlan.getQueryPlanningInfo() );
			}
			else {
				throw new IllegalArgumentException( "Unexpected type of operator: " + rootOp.getClass().getName() );
			}
		}

		if ( reqOps == null && mreqOps == null ) {
			// The plan did not contain any requests operator
			// for which we need to do cardinality requests.
			return;
		}

		// Now we are ready to add the cardinality estimates.
		addCardinalitiesForRequests(reqOps, infoObjs1, mreqOps, infoObjs2, ctx);
	}

	/**
	 * Makes sure that every nullary subplan within each of the
	 * given plans is annotated with cardinality estimates (which
	 * are determined by issuing cardinality requests).
	 */
	public void addCardinalitiesForRequests( final QueryProcContext ctx,
	                                         final PhysicalPlan ... plans ) {
		// Recursively extract all relevant nullary subplans from the
		// given plan, where a nullary subplan is considered relevant
		// if it does not yet have a cardinality estimate.
		final Set<PhysicalPlan> subPlans = extractNullarySubPlans(plans);

		// Without any relevant nullary subplans there is nothing to do here.
		if ( subPlans.isEmpty() ) return;

		// As input for method that adds cardinality estimates to the
		// extracted subplans, obtain the request operator and the
		// QueryPlanningInfo object of each of these subplans (and,
		// also, handle the special case of fixed-input operators).
		List<LogicalOpRequest<?,?>> reqOps  = null;
		List<LogicalOpMultiRequest> mreqOps = null;
		List<QueryPlanningInfo> infoObjs1 = null;
		List<QueryPlanningInfo> infoObjs2 = null;
		for ( final PhysicalPlan subPlan : subPlans )
		{
			final PhysicalOperator rootOp = subPlan.getRootOperator();
			if ( rootOp instanceof PhysicalOpMultiRequest mreqOp ) {
				if ( mreqOps == null ) {
					mreqOps   = new ArrayList<>( subPlans.size() );
					infoObjs2 = new ArrayList<>( subPlans.size() );
				}

				mreqOps.add( mreqOp.getLogicalOperator() );
				infoObjs2.add( subPlan.getQueryPlanningInfo() );
			}
			if ( rootOp instanceof PhysicalOpRequest reqOp ) {
				if ( reqOps == null ) {
					reqOps    = new ArrayList<>( subPlans.size() );
					infoObjs1 = new ArrayList<>( subPlans.size() );
				}

				reqOps.add( reqOp.getLogicalOperator() );
				infoObjs1.add( subPlan.getQueryPlanningInfo() );
			}
			else if ( rootOp instanceof PhysicalOpFixedSolMap ) {
				addCardinalityForFixedInputOps( subPlan.getQueryPlanningInfo() );
			}
			else {
				throw new IllegalArgumentException( "Unexpected type of operator: " + rootOp.getClass().getName() );
			}
		}

		// Now we are ready to add the cardinality estimates.
		addCardinalitiesForRequests(reqOps, infoObjs1, mreqOps, infoObjs2, ctx);
	}

	protected void addCardinalitiesForRequests( final List<LogicalOpRequest<?,?>> reqOps,
	                                            final List<QueryPlanningInfo> reqInfoObjs,
	                                            final List<LogicalOpMultiRequest> mreqOps,
	                                            final List<QueryPlanningInfo> mreqInfoObjs,
	                                            final QueryProcContext ctx ) {
		assert reqOps != null || mreqOps != null;
		assert reqOps  == null || ( reqInfoObjs != null && reqInfoObjs.size() == reqOps.size() );
		assert mreqOps == null || ( mreqInfoObjs != null && mreqInfoObjs.size() == mreqOps.size() );

		final CardinalityResponse[] resps;
		try {
			resps = FederationAccessUtils.performCardinalityRequests( ctx.getFederationAccessMgr(),
			                                                          reqOps,
			                                                          mreqOps );
		}
		catch ( final FederationAccessException e ) {
			// If the cardinality requests fail, we need to guess. For the
			// moment, we simply guess the worst possible cardinality (max).
// TODO: We should try to be a bit smarter, using some heuristic that takes
// the patterns of the given requests into account.
// TODO: Also, in cases in which only some of the cardinality requests failed,
// we should at least get the values for those that did not fail. 
			final QueryPlanProperty est = QueryPlanProperty.cardinality(Integer.MAX_VALUE,
			                                                            Quality.PURE_GUESS);
			final QueryPlanProperty min = QueryPlanProperty.minCardinality(0,
			                                                               Quality.MIN_OR_MAX_POSSIBLE);
			final QueryPlanProperty max = QueryPlanProperty.maxCardinality(Integer.MAX_VALUE,
			                                                               Quality.MIN_OR_MAX_POSSIBLE);
			if ( reqOps != null ) {
				for ( final QueryPlanningInfo qpInfo : reqInfoObjs ) {
					qpInfo.addProperty(est);
					qpInfo.addProperty(min);
					qpInfo.addProperty(max);
				}
			}

			if ( mreqOps != null ) {
				for ( final QueryPlanningInfo qpInfo : mreqInfoObjs ) {
					qpInfo.addProperty(est);
					qpInfo.addProperty(min);
					qpInfo.addProperty(max);
				}
			}
			return;
		}

		if ( reqOps != null ) {
			if ( resps.length < reqOps.size() )
				throw new IllegalStateException("Wrong number of cardinality responses (namely, " + resps.length + ", but at least " + reqOps.size() + " expected).");

			for ( int i = 0; i < reqOps.size(); i++ ) {
				final LogicalOpRequest<?,?> reqOp = reqOps.get(i);
				final QueryPlanningInfo infoObj = reqInfoObjs.get(i);

				final int cardValue;
				final int minCardValue;
				final int maxCardValue;
				final QueryPlanProperty.Quality cardQuality;
				final QueryPlanProperty.Quality minCardQuality;
				final QueryPlanProperty.Quality maxCardQuality;

				if ( resps[i].isError() ) {
					cardValue = Integer.MAX_VALUE;
					cardQuality = Quality.PURE_GUESS;
					minCardValue = 0;
					minCardQuality = Quality.MIN_OR_MAX_POSSIBLE;
					maxCardValue = Integer.MAX_VALUE;
					maxCardQuality = Quality.MIN_OR_MAX_POSSIBLE;
				}
				else {
					final int cardValueOfResponse;
					try {
						cardValueOfResponse = resps[i].getCardinality();
					}
					catch ( final Exception e ) {
						// We should not get an exception here because we are
						// in the branch where resps[i].isError() is false.
						throw new IllegalStateException();
					}

					// Check that the cardinality value retrieved via the
					// request is valid, where we consider any non-negative
					// integer a valid value.
					if ( cardValueOfResponse < 0 ) {
						// This is the case in which the value is invalid.
						cardValue = Integer.MAX_VALUE;
						cardQuality = Quality.PURE_GUESS;
						minCardValue = 0;
						minCardQuality = Quality.MIN_OR_MAX_POSSIBLE;
						maxCardValue = Integer.MAX_VALUE;
						maxCardQuality = Quality.MIN_OR_MAX_POSSIBLE;
					}
					else {
						// This is the case in which the value is valid.
						cardValue = cardValueOfResponse;
						minCardValue = cardValueOfResponse;
						maxCardValue = cardValueOfResponse;

						if ( reqOp.getFederationMember() instanceof SPARQLEndpoint )
							cardQuality = Quality.ACCURATE;
						else
							cardQuality = Quality.DIRECT_ESTIMATE;

						minCardQuality = cardQuality;
						maxCardQuality = cardQuality;
					}
				}

				infoObj.addProperty( QueryPlanProperty.cardinality(cardValue,
				                                                   cardQuality) );

				infoObj.addProperty( QueryPlanProperty.minCardinality(minCardValue,
				                                                      minCardQuality) );

				infoObj.addProperty( QueryPlanProperty.maxCardinality(maxCardValue,
				                                                      maxCardQuality) );
			}
		}

		if ( mreqOps == null || mreqOps.isEmpty() )
			return;

		int respIdx = (reqOps == null) ? -1 : reqOps.size() - 1;
		for ( int i = 0; i < mreqOps.size(); i++ ) {
			final LogicalOpMultiRequest mreqOp = mreqOps.get(i);

			int cardValue = 0; // to be updated in the loop below
			boolean errorFound = false;
			boolean nonSparqlEndpointFound = false;

			for ( final FederationMember fm : mreqOp.getFederationMembers() ) {
				if ( ! (fm instanceof SPARQLEndpoint) ) {
					nonSparqlEndpointFound = true;
				}

				respIdx++;
				if ( resps.length < respIdx - 1 )
					throw new IllegalStateException("Wrong number of cardinality responses (namely, " + resps.length + ", but at least " + respIdx + " expected).");

				if ( resps[respIdx].isError() ) {
					errorFound = true;
				}
				else {
					final int cardValueOfResponse;
					try {
						cardValueOfResponse = resps[respIdx].getCardinality();
					}
					catch ( final Exception e ) {
						// We should not get an exception here because we are
						// in the branch where resps[i].isError() is false.
						throw new IllegalStateException();
					}

					// Check that the cardinality value retrieved via the
					// request is valid, where we consider any non-negative
					// integer a valid value.
					if ( cardValueOfResponse < 0 ) {
						// This is the case in which the value is invalid.
						errorFound = true;
					}
					else {
						// This is the case in which the value is valid.
						cardValue += cardValueOfResponse;
					}
				}
			}

			final int minCardValue;
			final int maxCardValue;
			final QueryPlanProperty.Quality cardQuality;
			final QueryPlanProperty.Quality minCardQuality;
			final QueryPlanProperty.Quality maxCardQuality;
			if ( errorFound ) {
				cardValue    = Integer.MAX_VALUE;
				minCardValue = cardValue;
				maxCardValue = Integer.MAX_VALUE;
				cardQuality    = Quality.PURE_GUESS;
				minCardQuality = Quality.MIN_OR_MAX_POSSIBLE;
				maxCardQuality = Quality.MIN_OR_MAX_POSSIBLE;
			}
			else {
				if ( nonSparqlEndpointFound )
					cardQuality = Quality.DIRECT_ESTIMATE;
				else
					cardQuality = Quality.ACCURATE;

				minCardValue = cardValue;
				maxCardValue = cardValue;
				minCardQuality = cardQuality;
				maxCardQuality = cardQuality;
			}

			final QueryPlanningInfo infoObj = mreqInfoObjs.get(i);
			infoObj.addProperty( QueryPlanProperty.cardinality(cardValue,
			                                                   cardQuality) );
			infoObj.addProperty( QueryPlanProperty.minCardinality(minCardValue,
			                                                      minCardQuality) );
			infoObj.addProperty( QueryPlanProperty.maxCardinality(maxCardValue,
			                                                      maxCardQuality) );
		}
	}

	/**
	 * Populates the given {@link QueryPlanningInfo} object with cardinality
	 * information, assuming that this object is for a plan with a request
	 * operator that has a wrapped endpoint as its federation member.
	 */
	protected void addCardinalityForRequestViaWrapper( final QueryPlanningInfo qpInfo,
	                                                   final LogicalOpRequest<?,?> reqOp ) {
		assert reqOp.getFederationMember() instanceof WrappedRESTEndpoint;

		qpInfo.addProperty( QueryPlanProperty.cardinality(99, Quality.PURE_GUESS) );
		qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.MIN_OR_MAX_POSSIBLE) );
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(Integer.MAX_VALUE, Quality.MIN_OR_MAX_POSSIBLE) );
	}

	/**
	 * Populates the given {@link QueryPlanningInfo} object with cardinality
	 * information, assuming that this object is for a plan with a fixed-input
	 * operator (see {@link LogicalOpFixedSolMap}).
	 */
	protected void addCardinalityForFixedInputOps( final QueryPlanningInfo qpInfo ) {
		// Fixed-input operators produce exactly one solution mapping.
		qpInfo.addProperty( QueryPlanProperty.cardinality(1, Quality.ACCURATE) );
		qpInfo.addProperty( QueryPlanProperty.minCardinality(1, Quality.ACCURATE) );
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(1, Quality.ACCURATE) );
	}

	/**
	 * Returns all relevant nullary subplans that are somewhere within any
	 * of the given plans, where such a subplan is considered relevant if
	 * it does not yet have a cardinality estimate.
	 */
	protected Set<LogicalPlan> extractNullarySubPlans( final LogicalPlan ... plans ) {
		if ( plans.length == 0 )
			return Set.of();

		final Set<LogicalPlan> subPlans = new HashSet<>();
		for ( int i = 0; i < plans.length; i++ ) {
			extractNullarySubPlans( plans[i], subPlans );
		}

		return subPlans;
	}

	/**
	 * Returns all relevant nullary subplans that are somewhere within any
	 * of the given plans, where such a subplan is considered relevant if
	 * it does not yet have a cardinality estimate.
	 */
	protected Set<PhysicalPlan> extractNullarySubPlans( final PhysicalPlan ... plans ) {
		if ( plans.length == 0 )
			return Set.of();

		final Set<PhysicalPlan> subPlans = new HashSet<>();
		for ( int i = 0; i < plans.length; i++ ) {
			extractNullarySubPlans( plans[i], subPlans );
		}

		return subPlans;
	}

	/**
	 * Extracts all relevant nullary subplans that are somewhere within
	 * the given plan and adds these subplans to the given set, where
	 * such a subplan is considered relevant if it does not yet have a
	 * cardinality estimate.
	 */
	protected void extractNullarySubPlans( final LogicalPlan plan,
	                                       final Set<LogicalPlan> extractedSubPlans ) {
		if ( plan instanceof LogicalPlanWithNullaryRoot ) {
			final QueryPlanningInfo qpInfo = plan.getQueryPlanningInfo();
			if ( qpInfo.getProperty(CARDINALITY) == null ) {
				extractedSubPlans.add(plan);
			}
		}
		else {
			// Recursion: extract all relevant nullary plans
			// from the direct subplans of the given plan.
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				extractNullarySubPlans( plan.getSubPlan(i), extractedSubPlans );
			}
		}
	}

	/**
	 * Extracts all relevant nullary subplans that are somewhere within
	 * the given plan and adds these subplans to the given set, where
	 * such a subplan is considered relevant if it does not yet have a
	 * cardinality estimate.
	 */
	protected void extractNullarySubPlans( final PhysicalPlan plan,
	                                       final Set<PhysicalPlan> extractedSubPlans ) {
		if ( plan instanceof PhysicalPlanWithNullaryRoot ) {
			final QueryPlanningInfo qpInfo = plan.getQueryPlanningInfo();
			if ( qpInfo.getProperty(CARDINALITY) == null ) {
				extractedSubPlans.add(plan);
			}
		}
		else {
			// Recursion: extract all relevant nullary plans
			// from the direct subplans of the given plan.
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				extractNullarySubPlans( plan.getSubPlan(i), extractedSubPlans );
			}
		}
	}

}
