package se.liu.ida.hefquin.engine.queryproc.impl.cardinality;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Quality;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNullaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithNullaryRoot;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryproc.CardinalityEstimator;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;

import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.CARDINALITY;

public class RequestBasedCardinalityEstimator implements CardinalityEstimator
{
	protected final FederationAccessManager fedAccMgr;

	public RequestBasedCardinalityEstimator( final FederationAccessManager fedAccMgr ) {
		assert fedAccMgr != null;
		this.fedAccMgr = fedAccMgr;
	}

	@Override
	public void addCardinalities( final LogicalPlan ... plans ) {
		// As a first step, make sure that all nullary subplans (i.e.,
		// with request operators) within the given plans are annotated
		// with a cardinality estimate.
		addCardinalitiesForRequests(plans);

		// Now, use the worker to determine the cardinality estimates
		// for the given plans recursively. 
		new CardinalityEstimationWorkerImpl().addCardinalities(plans);
	}

	@Override
	public void addCardinalities( final PhysicalPlan ... plans ) {
		// As a first step, make sure that all nullary subplans (i.e.,
		// with request operators) within the given plans are annotated
		// with a cardinality estimate.
		addCardinalitiesForRequests(plans);

		// Now, use the worker to determine the cardinality estimates
		// for the given plans recursively. 
		new CardinalityEstimationWorkerImpl().addCardinalities(plans);
	}


	/**
	 * Makes sure that every nullary subplan within each of the
	 * given plans is annotated with cardinality estimates (which
	 * are determined by issuing cardinality requests).
	 */
	public void addCardinalitiesForRequests( final LogicalPlan ... plans ) {
		// Recursively extract all relevant nullary subplans from the
		// given plan, where a nullary subplan is considered relevant
		// if it does not yet have a cardinality estimate.
		final Set<LogicalPlan> subPlans = extractNullarySubPlans(plans);

		// Without any relevant nullary subplans there is nothing to do here.
		if ( subPlans.isEmpty() ) return;

		// As input for method that adds cardinality estimates to the
		// extracted subplans, obtain the request operator and the
		// QueryPlanningInfo object of each of these subplans.
		final List<LogicalOpRequest<?,?>> reqOps  = new ArrayList<>( subPlans.size() );
		final List<QueryPlanningInfo> infoObjs    = new ArrayList<>( subPlans.size() );
		for ( final LogicalPlan subPlan : subPlans ) {
			reqOps.add( (LogicalOpRequest<?,?>) subPlan.getRootOperator() );
			infoObjs.add( subPlan.getQueryPlanningInfo() );
		}

		// Now we are ready to add the cardinality estimates.
		addCardinalitiesForRequests(reqOps, infoObjs);
	}

	/**
	 * Makes sure that every nullary subplan within each of the
	 * given plans is annotated with cardinality estimates (which
	 * are determined by issuing cardinality requests).
	 */
	public void addCardinalitiesForRequests( final PhysicalPlan ... plans ) {
		// Recursively extract all relevant nullary subplans from the
		// given plan, where a nullary subplan is considered relevant
		// if it does not yet have a cardinality estimate.
		final Set<PhysicalPlan> subPlans = extractNullarySubPlans(plans);

		// Without any relevant nullary subplans there is nothing to do here.
		if ( subPlans.isEmpty() ) return;

		// As input for method that adds cardinality estimates to the
		// extracted subplans, obtain the request operator and the
		// QueryPlanningInfo object of each of these subplans.
		final List<LogicalOpRequest<?,?>> reqOps  = new ArrayList<>( subPlans.size() );
		final List<QueryPlanningInfo> infoObjs    = new ArrayList<>( subPlans.size() );
		for ( final PhysicalPlan subPlan : subPlans ) {
			final PhysicalOpRequest<?,?> popReq = (PhysicalOpRequest<?,?>) subPlan.getRootOperator();
			reqOps.add( popReq.getLogicalOperator() );
			infoObjs.add( subPlan.getQueryPlanningInfo() );
		}

		// Now we are ready to add the cardinality estimates.
		addCardinalitiesForRequests(reqOps, infoObjs);
	}

	protected void addCardinalitiesForRequests( final List<LogicalOpRequest<?,?>> reqOps,
	                                            final List<QueryPlanningInfo> infoObjs ) {
		final CardinalityResponse[] resps;
		try {
			resps = FederationAccessUtils.performCardinalityRequests( fedAccMgr,
			                                                          reqOps );
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
			for ( final QueryPlanningInfo qpInfo : infoObjs ) {
				qpInfo.addProperty(est);
				qpInfo.addProperty(min);
				qpInfo.addProperty(max);
			}
			return;
		}

		if ( resps.length != reqOps.size() )
			throw new IllegalStateException("Wrong number of cardinality responses (namely, " + resps.length + ", but " + reqOps.size() + " expected).");

		for ( int i = 0; i < resps.length; i++ ) {
			final LogicalOpRequest<?,?> reqOp = reqOps.get(i);
			final QueryPlanningInfo infoObj = infoObjs.get(i);

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
